package com.bigPicture.businessreportgenerator.presentation.features.portfolio

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigPicture.businessreportgenerator.data.domain.Asset
import com.bigPicture.businessreportgenerator.data.domain.AssetType
import com.bigPicture.businessreportgenerator.data.domain.ExchangeRate
import com.bigPicture.businessreportgenerator.data.domain.StockHistoryItem
import com.bigPicture.businessreportgenerator.data.local.repository.AssetRepository
import com.bigPicture.businessreportgenerator.data.remote.api.FinanceApiService
import com.bigPicture.businessreportgenerator.data.remote.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class TickerValidationResult(
    val isValid: Boolean,
    val actualTicker: String? = null,
    val companyName: String? = null,
    val errorMessage: String? = null
)

data class ExchangeRateInfo(
    val usdToKrw: Double = 1300.0,
    val lastUpdated: Long? = null,
    val isLoading: Boolean = false
)

data class ModernPortfolioState(
    val assets: List<Asset> = emptyList(),
    val sampleAssets: List<Asset> = emptyList(),
    val isAddAssetDialogVisible: Boolean = false,
    val isSamplePortfolioDialogVisible: Boolean = false,
    val totalPortfolioValue: Double = 0.0,
    val totalReturnValue: Double = 0.0,
    val totalReturnPercentage: Double = 0.0,
    val isLoading: Boolean = false,
    val currentInvestmentTip: InvestmentTip? = null,
    // 티커 검증 관련 상태들
    val isValidatingTicker: Boolean = false,
    val tickerValidationResult: TickerValidationResult? = null,
    val isLoadingPrices: Boolean = false,
    // 환율 정보 추가
    val exchangeRateInfo: ExchangeRateInfo = ExchangeRateInfo()
)

class PortfolioViewModel(
    private val repository: AssetRepository,
    private val financeApiService: FinanceApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ModernPortfolioState())
    val state: StateFlow<ModernPortfolioState> = _state.asStateFlow()

    // 현재 환율 (실시간으로 업데이트됨)
    private var currentExchangeRate = ExchangeRate(1300.0)

    init {
        loadAssets()
        setRandomInvestmentTip()
        fetchExchangeRate() // 환율 정보 가져오기
    }

    // 환율 정보 가져오기
    fun fetchExchangeRate() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(exchangeRateInfo = it.exchangeRateInfo.copy(isLoading = true))
            }

            try {
                val responseExchange = RetrofitClient.ExchangeService.getExchangeUs()
                if (responseExchange.isSuccessful) {
                    val koExData = responseExchange.body()
                    if (koExData != null) {
                        // 날짜가 최신인 값을 선택
                        val latest = koExData.data.maxByOrNull { it.ExchangeDate }
                        val newRate = latest?.ExchangeRate ?: 1300.0

                        // ExchangeRate 객체와 상태 업데이트
                        currentExchangeRate = ExchangeRate(newRate)

                        _state.update {
                            it.copy(
                                exchangeRateInfo = ExchangeRateInfo(
                                    usdToKrw = newRate,
                                    lastUpdated = System.currentTimeMillis(),
                                    isLoading = false
                                )
                            )
                        }

                        // 환율이 업데이트되면 자산 가치 재계산
                        recalculatePortfolioValues()

                        Log.d("PortfolioViewModel", "환율 정보 업데이트 성공: $newRate")
                    }
                } else {
                    Log.e("PortfolioViewModel", "환율 정보 불러오기 실패")
                    _state.update {
                        it.copy(exchangeRateInfo = it.exchangeRateInfo.copy(isLoading = false))
                    }
                }
            } catch (e: Exception) {
                Log.e("PortfolioViewModel", "환율 정보 불러오기 예외: ${e.message}", e)
                _state.update {
                    it.copy(exchangeRateInfo = it.exchangeRateInfo.copy(isLoading = false))
                }
            }
        }
    }

    // 포트폴리오 가치 재계산
    private fun recalculatePortfolioValues() {
        val currentAssets = _state.value.assets
        val totalPurchaseValue = currentAssets.sumOf { it.purchasePrice }
        val totalCurrentValue = currentAssets.sumOf {
            it.getCurrentValueInKRW(currentExchangeRate) ?: it.purchasePrice
        }
        val totalReturn = totalCurrentValue - totalPurchaseValue
        val totalReturnPercentage = if (totalPurchaseValue > 0) {
            (totalReturn / totalPurchaseValue) * 100
        } else 0.0

        _state.update {
            it.copy(
                totalPortfolioValue = totalCurrentValue,
                totalReturnValue = totalReturn,
                totalReturnPercentage = totalReturnPercentage
            )
        }
    }

    private fun loadAssets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.getAllAssets().collect { assets ->
                // 주식 타입의 자산들의 가격을 업데이트
                val updatedAssets = updateStockPrices(assets)

                val totalPurchaseValue = updatedAssets.sumOf { it.purchasePrice }
                val totalCurrentValue = updatedAssets.sumOf {
                    it.getCurrentValueInKRW(currentExchangeRate) ?: it.purchasePrice
                }
                val totalReturn = totalCurrentValue - totalPurchaseValue
                val totalReturnPercentage = if (totalPurchaseValue > 0) {
                    (totalReturn / totalPurchaseValue) * 100
                } else 0.0

                _state.update {
                    it.copy(
                        assets = updatedAssets,
                        totalPortfolioValue = totalCurrentValue,
                        totalReturnValue = totalReturn,
                        totalReturnPercentage = totalReturnPercentage,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun updateStockPrices(assets: List<Asset>): List<Asset> {
        return assets.map { asset ->
            if (asset.type == AssetType.STOCK && asset.ticker != null) {
                // 가격이 1시간 이상 오래되었거나 없으면 업데이트
                val shouldUpdate = asset.lastUpdated?.let { lastUpdate ->
                    System.currentTimeMillis() - lastUpdate > TimeUnit.HOURS.toMillis(1)
                } ?: true

                if (shouldUpdate) {
                    try {
                        val priceResponse = financeApiService.getTodayStockPrice(asset.ticker)
                        if (priceResponse.status == "OK") {
                            asset.copy(
                                currentPrice = priceResponse.data.stockPrice,
                                lastUpdated = System.currentTimeMillis()
                            )
                        } else asset
                    } catch (e: Exception) {
                        // 네트워크 오류 등의 경우 기존 가격 유지
                        asset
                    }
                } else asset
            } else asset
        }
    }

    // 환율 새로고침
    fun refreshExchangeRate() {
        fetchExchangeRate()
    }

    // 현재 ExchangeRate 객체 반환 (다이얼로그에서 사용)
    fun getCurrentExchangeRate(): ExchangeRate {
        return currentExchangeRate
    }

    // 개선된 티커 유효성 검사
    fun validateTicker(ticker: String) {
        viewModelScope.launch {
            _state.update { it.copy(isValidatingTicker = true, tickerValidationResult = null) }

            try {
                val response = financeApiService.checkTicker(ticker)

                if (response.status == "OK" && response.data == true) {
                    // API에서 단순히 true/false만 반환하므로 입력된 티커가 유효함을 의미
                    val result = TickerValidationResult(
                        isValid = true,
                        actualTicker = ticker, // 입력된 티커가 유효함
                        companyName = ticker // 회사명 정보가 없으므로 티커를 표시
                    )
                    _state.update {
                        it.copy(
                            isValidatingTicker = false,
                            tickerValidationResult = result
                        )
                    }
                } else {
                    val result = TickerValidationResult(
                        isValid = false,
                        errorMessage = "유효하지 않은 티커입니다: $ticker"
                    )
                    _state.update {
                        it.copy(
                            isValidatingTicker = false,
                            tickerValidationResult = result
                        )
                    }
                }
            } catch (e: Exception) {
                val result = TickerValidationResult(
                    isValid = false,
                    errorMessage = "티커 검증 중 오류가 발생했습니다"
                )
                _state.update {
                    it.copy(
                        isValidatingTicker = false,
                        tickerValidationResult = result
                    )
                }
            }
        }
    }

    // 티커 검증 상태 초기화
    fun clearTickerValidation() {
        _state.update {
            it.copy(tickerValidationResult = null)
        }
    }

    // 주식 가격 조회
    suspend fun getStockPrice(ticker: String): Double? {
        return try {
            val response = financeApiService.getTodayStockPrice(ticker)
            if (response.status == "OK") {
                response.data.stockPrice
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun addAsset(asset: Asset) {
        viewModelScope.launch {
            try {
                // 주식인 경우 현재 가격도 함께 조회해서 저장
                val finalAsset = if (asset.type == AssetType.STOCK && asset.ticker != null) {
                    val currentPrice = getStockPrice(asset.ticker)
                    asset.copy(
                        currentPrice = currentPrice,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    asset
                }

                repository.insertAsset(finalAsset)
                hideAddAssetDialog()
            } catch (e: Exception) {
                // 에러 처리
                hideAddAssetDialog()
            }
        }
    }

    fun removeAsset(asset: Asset) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun refreshStockPrices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPrices = true) }

            val currentAssets = _state.value.assets
            val stockAssets = currentAssets.filter { it.type == AssetType.STOCK && it.ticker != null }

            stockAssets.forEach { asset ->
                try {
                    val currentPrice = getStockPrice(asset.ticker!!)
                    val updatedAsset = asset.copy(
                        currentPrice = currentPrice,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updateAsset(updatedAsset)
                } catch (e: Exception) {
                    // 개별 주식 가격 조회 실패는 무시하고 계속 진행
                }
            }

            _state.update { it.copy(isLoadingPrices = false) }
        }
    }

    private fun setRandomInvestmentTip() {
        // 샘플 InvestmentTip 생성
        val sampleTip = InvestmentTip(
            title = "분산 투자의 힘",
            description = "여러 종목에 분산 투자하여 리스크를 줄이고 안정적인 수익을 추구하세요.",
            icon = Icons.Rounded.Star,
            iconColor = Color.White,
            gradient = Brush.linearGradient(
                listOf(
                    Color(0xFF667eea),
                    Color(0xFF764ba2)
                )
            )
        )

        _state.update {
            it.copy(currentInvestmentTip = sampleTip)
        }
    }

    fun showAddAssetDialog() {
        _state.update {
            it.copy(
                isAddAssetDialogVisible = true,
                tickerValidationResult = null
            )
        }
    }

    fun hideAddAssetDialog() {
        _state.update {
            it.copy(
                isAddAssetDialogVisible = false,
                tickerValidationResult = null
            )
        }
    }

    // 기존 메서드는 호환성을 위해 유지
    fun clearTickerValidationError() {
        clearTickerValidation()
    }

    fun showSamplePortfolioDialog() {
        _state.update {
            it.copy(
                isSamplePortfolioDialogVisible = true,
                sampleAssets = ModernPortfolioSampleData.samplePortfolios
            )
        }
    }

    fun hideSamplePortfolioDialog() {
        _state.update { it.copy(isSamplePortfolioDialogVisible = false) }
    }

    fun loadSamplePortfolio() {
        viewModelScope.launch {
            // 기존 자산들 삭제
            _state.value.assets.forEach { asset ->
                repository.deleteAsset(asset)
            }

            // 샘플 자산들 추가 (티커와 현재 가격 포함)
            ModernPortfolioSampleData.samplePortfolios.forEach { asset ->
                val finalAsset = if (asset.type == AssetType.STOCK && asset.ticker != null) {
                    val currentPrice = getStockPrice(asset.ticker)
                    asset.copy(
                        currentPrice = currentPrice,
                        lastUpdated = System.currentTimeMillis()
                    )
                } else {
                    asset
                }
                repository.insertAsset(finalAsset)
            }
        }
    }

    fun refreshInvestmentTip() {
        setRandomInvestmentTip()
    }

    suspend fun getStockHistory(ticker: String): List<StockHistoryItem> {
        return try {

            val response = financeApiService.getStockHistory(ticker)

            Log.d("PortfolioViewModel", "API 응답 받음: status=${response.status}")
            Log.d("PortfolioViewModel", "API 응답 코드: ${response.code}")
            Log.d("PortfolioViewModel", "데이터 개수: ${response.data.size}")

            if (response.status == "OK") {
                Log.d("PortfolioViewModel", "성공: ${response.data.size}개 데이터 반환")
                response.data
            } else {
                Log.w("PortfolioViewModel", "API 상태 실패: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("PortfolioViewModel", "예외 타입: ${e.javaClass.simpleName}")
            Log.e("PortfolioViewModel", "예외 메시지: ${e.message}")
            Log.e("PortfolioViewModel", "스택 트레이스:", e)
            emptyList()
        }
    }
}