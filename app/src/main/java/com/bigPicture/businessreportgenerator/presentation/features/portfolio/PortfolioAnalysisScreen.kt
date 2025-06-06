package com.bigPicture.businessreportgenerator.presentation.features.portfolio

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bigPicture.businessreportgenerator.data.domain.Asset
import com.bigPicture.businessreportgenerator.data.domain.AssetType
import com.bigPicture.businessreportgenerator.data.domain.Currency
import com.bigPicture.businessreportgenerator.data.domain.ExchangeRate
import com.bigPicture.businessreportgenerator.data.domain.PortfolioAnalysisData
import com.bigPicture.businessreportgenerator.data.domain.StockHistoryItem
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale


@Composable
fun PortfolioAnalysisScreen(
    assets: List<Asset>,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portfolioViewModel: PortfolioViewModel = koinViewModel()
    val portfolioState by portfolioViewModel.state.collectAsState()

    var analysisData by remember { mutableStateOf<List<PortfolioAnalysisData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var totalPortfolioHistory by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 실시간 환율 사용
    val exchangeRate = remember(portfolioState.exchangeRateInfo.usdToKrw) {
        ExchangeRate(portfolioState.exchangeRateInfo.usdToKrw)
    }

    // 주식 타입 자산만 필터링
    val stockAssets = remember(assets) {
        assets.filter { it.type == AssetType.STOCK && !it.ticker.isNullOrEmpty() }
    }

    Log.d("PortfolioAnalysis", "분석할 주식 수: ${stockAssets.size}")
    Log.d("PortfolioAnalysis", "사용 중인 환율: ${exchangeRate.rate}")

    LaunchedEffect(stockAssets, exchangeRate.rate) {
        if (stockAssets.isEmpty()) {
            isLoading = false
            errorMessage = "분석할 주식이 없습니다. 주식을 추가해주세요."
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null

        try {
            val analysisResults = mutableListOf<PortfolioAnalysisData>()
            val portfolioValueByDate = mutableMapOf<String, Double>()

            for (asset in stockAssets) {
                try {
                    Log.d("PortfolioAnalysis", "주식 히스토리 조회 중: ${asset.ticker}")
                    Log.d("PortfolioAnalysis", "Asset details: ${asset.details}")

                    val history = portfolioViewModel.getStockHistory(asset.ticker!!)

                    if (history.isNotEmpty()) {
                        val currentPrice = history.lastOrNull()?.stockPrice ?: 0.0

                        // Asset 클래스의 확장 속성 사용 (details에서 가져옴)
                        val purchasePricePerShare = asset.purchasePricePerShare ?: 0.0
                        val shares = asset.shares ?: 1.0

                        Log.d("PortfolioAnalysis", "${asset.name}:")
                        Log.d("PortfolioAnalysis", "  - purchasePricePerShare: $purchasePricePerShare")
                        Log.d("PortfolioAnalysis", "  - shares: $shares")
                        Log.d("PortfolioAnalysis", "  - currentPrice: $currentPrice")
                        Log.d("PortfolioAnalysis", "  - market: ${asset.market}")

                        // 포트폴리오 화면과 동일한 계산 방식 사용 (실시간 환율 적용)
                        val currentValueInKRW = asset.getCurrentValueInKRW(exchangeRate) ?: asset.purchasePrice
                        val purchaseValueInKRW = asset.purchasePrice // 이미 원화로 저장됨

                        val returnAmount = currentValueInKRW - purchaseValueInKRW
                        val returnPercentage = if (purchaseValueInKRW > 0) (returnAmount / purchaseValueInKRW) * 100 else 0.0

                        Log.d("PortfolioAnalysis", "  - currentValueInKRW: $currentValueInKRW")
                        Log.d("PortfolioAnalysis", "  - purchaseValueInKRW: $purchaseValueInKRW")
                        Log.d("PortfolioAnalysis", "  - returnAmount: $returnAmount")

                        analysisResults.add(
                            PortfolioAnalysisData(
                                asset = asset,
                                priceHistory = history,
                                currentValue = currentValueInKRW,
                                purchaseValue = purchaseValueInKRW,
                                returnPercentage = returnPercentage,
                                returnAmount = returnAmount
                            )
                        )

                        // 포트폴리오 전체 가치 계산 (실시간 환율로 원화 환산)
                        history.forEach { historyItem ->
                            val dateValueInMarketCurrency = historyItem.stockPrice * shares
                            val dateValueInKRW = when (asset.market?.currency) {
                                Currency.KRW -> dateValueInMarketCurrency
                                Currency.USD -> exchangeRate.usdToKrw(dateValueInMarketCurrency)
                                null -> dateValueInMarketCurrency
                            }
                            portfolioValueByDate[historyItem.stockDate] =
                                portfolioValueByDate.getOrDefault(historyItem.stockDate, 0.0) + dateValueInKRW
                        }

                        Log.d("PortfolioAnalysis", "${asset.name} 분석 완료:")
                        Log.d("PortfolioAnalysis", "  - 현재가치(원화): $currentValueInKRW")
                        Log.d("PortfolioAnalysis", "  - 매입가치(원화): $purchaseValueInKRW")
                        Log.d("PortfolioAnalysis", "  - 수익률: ${returnPercentage}%")
                    }
                } catch (e: Exception) {
                    Log.e("PortfolioAnalysis", "${asset.ticker} 분석 실패: ${e.message}")
                    e.printStackTrace()
                }
            }

            analysisData = analysisResults
            totalPortfolioHistory = portfolioValueByDate.toList().sortedBy { it.first }

            val totalCurrentValue = analysisResults.sumOf { it.currentValue }
            val totalPurchaseValue = analysisResults.sumOf { it.purchaseValue }
            val totalReturnAmount = analysisResults.sumOf { it.returnAmount }

            Log.d("PortfolioAnalysis", "=== 분석 완료 (실시간 환율 적용) ===")
            Log.d("PortfolioAnalysis", "사용된 환율: ${exchangeRate.rate}")
            Log.d("PortfolioAnalysis", "총 현재 가치: $totalCurrentValue")
            Log.d("PortfolioAnalysis", "총 매입 가치: $totalPurchaseValue")
            Log.d("PortfolioAnalysis", "총 수익금: $totalReturnAmount")

        } catch (e: Exception) {
            Log.e("PortfolioAnalysis", "전체 분석 실패: ${e.message}")
            e.printStackTrace()
            errorMessage = "분석 중 오류가 발생했습니다: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FAFF))
    ) {
        when {
            isLoading -> {
                LoadingScreen(stockAssets.size)
            }

            errorMessage != null -> {
                ErrorScreen(errorMessage!!, onBackPressed)
            }

            analysisData.isEmpty() -> {
                EmptyDataScreen(onBackPressed)
            }

            else -> {
                AnalysisContent(
                    analysisData = analysisData,
                    totalPortfolioHistory = totalPortfolioHistory,
                    exchangeRate = exchangeRate
                )
            }
        }

        // 플로팅 백 버튼
        FloatingActionButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .size(48.dp),
            containerColor = Color.White.copy(alpha = 0.9f),
            contentColor = Color(0xFF1A1A2E),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun LoadingScreen(stockCount: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF667eea),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "포트폴리오 분석 중...",
                fontSize = 16.sp,
                color = Color(0xFF8E8E93),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${stockCount}개 주식 데이터 수집",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ErrorScreen(errorMessage: String, onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "에러",
                tint = Color(0xFFFF3B30),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                fontSize = 16.sp,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBackPressed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                )
            ) {
                Text("돌아가기", color = Color.White)
            }
        }
    }
}

@Composable
private fun EmptyDataScreen(onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Face,
                contentDescription = "데이터 없음",
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "분석할 주식 데이터가 없습니다",
                fontSize = 18.sp,
                color = Color(0xFF1A1A2E),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "주식을 추가하고 다시 시도해주세요",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBackPressed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                )
            ) {
                Text("돌아가기", color = Color.White)
            }
        }
    }
}

@Composable
private fun AnalysisContent(
    analysisData: List<PortfolioAnalysisData>,
    totalPortfolioHistory: List<Pair<String, Double>>,
    exchangeRate: ExchangeRate
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 헤더
        item {
            AnalysisHeaderSection(
                totalAssets = analysisData.size,
                totalValue = analysisData.sumOf { it.currentValue },
                totalReturn = analysisData.sumOf { it.returnAmount },
                totalReturnPercentage = run {
                    val totalPurchase = analysisData.sumOf { it.purchaseValue }
                    val totalCurrent = analysisData.sumOf { it.currentValue }
                    if (totalPurchase > 0) ((totalCurrent - totalPurchase) / totalPurchase) * 100 else 0.0
                },
                exchangeRate = exchangeRate
            )
        }

        // 전체 포트폴리오 차트
        if (totalPortfolioHistory.isNotEmpty()) {
            item {
                PortfolioTotalValueChart(
                    portfolioHistory = totalPortfolioHistory
                )
            }
        }

        // 개별 자산 분석
        items(analysisData) { data ->
            IndividualAssetAnalysisCard(
                analysisData = data,
                exchangeRate = exchangeRate
            )
        }
    }
}

@Composable
fun AnalysisHeaderSection(
    totalAssets: Int,
    totalValue: Double,
    totalReturn: Double,
    totalReturnPercentage: Double,
    exchangeRate: ExchangeRate
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val isProfit = totalReturn >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isProfit) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFf093fb),
                                Color(0xFFf5576c)
                            )
                        )
                    },
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "포트폴리오 분석",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-0.6).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "3개월 주가 변화 분석",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 환율 정보 표시
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "환율 적용",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${String.format("%.2f", exchangeRate.rate)} KRW/USD",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "총 자산가치",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatter.format(totalValue),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "총 수익률",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${if (isProfit) "+" else ""}${String.format("%.2f", totalReturnPercentage)}%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "분석 대상: ${totalAssets}개 종목",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${if (isProfit) "+" else ""}${formatter.format(totalReturn)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

data class ChartColors(
    val primary: Color,
    val secondary: Color,
    val gradient: List<Color>,
    val gridColor: Color,
    val textColor: Color
)

@Composable
fun PortfolioTotalValueChart(
    portfolioHistory: List<Pair<String, Double>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Enhanced Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📈 포트폴리오 가치 변화",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1A2E),
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "최근 3개월 총 자산 가치 추이",
                        fontSize = 14.sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Performance indicator
                val firstValue = portfolioHistory.firstOrNull()?.second ?: 0.0
                val lastValue = portfolioHistory.lastOrNull()?.second ?: 0.0
                val performance = if (firstValue > 0) ((lastValue - firstValue) / firstValue) * 100 else 0.0
                val isPositive = performance >= 0

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isPositive) Color(0xFF10B981).copy(alpha = 0.1f)
                            else Color(0xFFEF4444).copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.2f", performance)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8FAFF),
                                Color(0xFFFFFFFF)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                PortfolioLineChart(
                    data = portfolioHistory.associate { it.first to it.second.toFloat() },
                    isPortfolioChart = true
                )
            }
        }
    }
}

@Composable
fun IndividualAssetAnalysisCard(
    analysisData: PortfolioAnalysisData,
    exchangeRate: ExchangeRate
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val isProfit = analysisData.returnAmount >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Enhanced Header with Stock Symbol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Company Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6).copy(alpha = 0.2f),
                                        Color(0xFF7C3AED).copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = analysisData.asset.name.take(2),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8B5CF6),
                            letterSpacing = (-0.2).sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = analysisData.asset.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                            letterSpacing = (-0.3).sp
                        )
                        analysisData.asset.ticker?.let { ticker ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📊 $ticker",
                                    fontSize = 14.sp,
                                    color = Color(0xFF8E8E93),
                                    fontWeight = FontWeight.Medium
                                )
                                analysisData.asset.market?.let { market ->
                                    if (market.currency == Currency.USD) {
                                        Text(
                                            text = " • ${String.format("%.0f", exchangeRate.rate)} KRW/USD",
                                            fontSize = 12.sp,
                                            color = Color(0xFF8E8E93),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isProfit) "+" else ""}${String.format("%.2f", analysisData.returnPercentage)}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "${if (isProfit) "+" else ""}${formatter.format(analysisData.returnAmount)}",
                        fontSize = 14.sp,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Enhanced Value Information with Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF8FAFF),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "현재 가치",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatter.format(analysisData.currentValue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "매입 가치",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatter.format(analysisData.purchaseValue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Enhanced Individual Asset Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF0F9FF),
                                Color(0xFFFFFFFF)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                AssetPriceChart(
                    priceHistory = analysisData.priceHistory,
                    shares = analysisData.asset.shares ?: 1.0,
                    asset = analysisData.asset,
                    purchaseValue = analysisData.purchaseValue.toFloat(), // 매입가치 전달
                    exchangeRate = exchangeRate
                )
            }
        }
    }
}

@Composable
fun PortfolioLineChart(
    data: Map<String, Float>,
    isPortfolioChart: Boolean = false,
    purchaseValue: Float? = null // 매입가치 기준선 추가
) {
    if (data.isEmpty() || data.size < 2) return

    val values = data.values.toList()
    val dates = data.keys.toList()
    val dataMax = values.maxOrNull() ?: 0f
    val dataMin = values.minOrNull() ?: 0f

    // 매입가치 기준선이 보이도록 범위 조정
    val adjustedMax: Float
    val adjustedMin: Float

    if (purchaseValue != null && !isPortfolioChart) {
        // 매입가치를 중심으로 상하 20% 여백 확보
        val currentValue = values.lastOrNull() ?: purchaseValue
        val maxValue = maxOf(dataMax, purchaseValue, currentValue)
        val minValue = minOf(dataMin, purchaseValue, currentValue)
        val valueRange = maxValue - minValue

        // 최소 범위 보장 (매입가치의 10%)
        val minRange = purchaseValue * 0.1f
        val finalRange = maxOf(valueRange, minRange)

        // 매입가치가 차트 중앙 근처에 오도록 조정
        val centerValue = (maxValue + minValue) / 2f
        val halfRange = finalRange * 0.6f // 좀 더 여유있게

        adjustedMax = centerValue + halfRange
        adjustedMin = centerValue - halfRange
    } else {
        adjustedMax = dataMax
        adjustedMin = dataMin
    }

    val range = (adjustedMax - adjustedMin).takeIf { it != 0f } ?: 1f

    // Different colors for portfolio vs individual charts
    val chartColors = if (isPortfolioChart) {
        ChartColors(
            primary = Color(0xFF10B981),
            secondary = Color(0xFF059669),
            gradient = listOf(
                Color(0xFF10B981).copy(alpha = 0.3f),
                Color(0xFF10B981).copy(alpha = 0.05f)
            ),
            gridColor = Color(0xFFE5E7EB),
            textColor = Color(0xFF6B7280)
        )
    } else {
        ChartColors(
            primary = Color(0xFF8B5CF6),
            secondary = Color(0xFF7C3AED),
            gradient = listOf(
                Color(0xFF8B5CF6).copy(alpha = 0.3f),
                Color(0xFF8B5CF6).copy(alpha = 0.05f)
            ),
            gridColor = Color(0xFFE5E7EB),
            textColor = Color(0xFF6B7280)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width - 80.dp.toPx() // Space for Y-axis labels
        val height = size.height - 60.dp.toPx() // Space for X-axis labels
        val startX = 60.dp.toPx()
        val startY = 30.dp.toPx()

        val stepX = width / (values.size - 1)

        // Grid Lines with Y-axis labels
        for (i in 0..4) {
            val y = startY + (height / 4) * i
            val value = adjustedMax - (range / 4) * i

            // Horizontal grid lines
            drawLine(
                color = chartColors.gridColor,
                start = Offset(startX, y),
                end = Offset(startX + width, y),
                strokeWidth = 1.dp.toPx()
            )

            // Y-axis labels (values)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = chartColors.textColor.toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }

                val formattedValue = if (isPortfolioChart) {
                    // Portfolio values in millions/thousands
                    when {
                        value >= 1000000 -> "${(value / 1000000).toInt()}M"
                        value >= 1000 -> "${(value / 1000).toInt()}K"
                        else -> value.toInt().toString()
                    }
                } else {
                    // Individual stock prices
                    if (value >= 1000) "${(value / 1000).toInt()}K" else String.format("%.0f", value)
                }

                drawText(
                    formattedValue,
                    startX - 15.dp.toPx(),
                    y + 8.dp.toPx(),
                    paint
                )
            }
        }

        // Vertical grid lines and X-axis labels (dates)
        val dateIndices = listOf(0, values.size / 2, values.size - 1)
        dateIndices.forEach { index ->
            if (index < values.size) {
                val x = startX + index * stepX

                // Vertical grid lines
                drawLine(
                    color = chartColors.gridColor.copy(alpha = 0.5f),
                    start = Offset(x, startY),
                    end = Offset(x, startY + height),
                    strokeWidth = 1.dp.toPx()
                )

                // X-axis labels (dates)
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = chartColors.textColor.toArgb()
                        textSize = 26f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val dateText = dates[index].substring(5) // MM-DD format
                    drawText(
                        dateText,
                        x,
                        startY + height + 30.dp.toPx(),
                        paint
                    )
                }
            }
        }

        // Data points calculation
        val points = values.mapIndexed { index, value ->
            val x = startX + index * stepX
            val y = startY + height - ((value - adjustedMin) / range) * height
            Offset(x, y)
        }

        // 매입가치 기준선 그리기 (개별 종목 차트에만)
        if (!isPortfolioChart && purchaseValue != null) {
            val purchaseY = startY + height - ((purchaseValue - adjustedMin) / range) * height

            // 매입가치 배경 영역 (손익 구분)
            val currentValue = values.lastOrNull() ?: purchaseValue
            val isProfit = currentValue >= purchaseValue

            // 매입가치와 현재가치 사이 영역 채우기
            val currentY = points.lastOrNull()?.y ?: purchaseY
            if (kotlin.math.abs(currentY - purchaseY) > 1.dp.toPx()) {
                val backgroundPath = Path().apply {
                    moveTo(startX, purchaseY)
                    lineTo(startX + width, purchaseY)
                    lineTo(startX + width, currentY)
                    lineTo(startX, currentY)
                    close()
                }

                drawPath(
                    path = backgroundPath,
                    color = if (isProfit) {
                        Color(0xFF10B981).copy(alpha = 0.08f)
                    } else {
                        Color(0xFFEF4444).copy(alpha = 0.08f)
                    }
                )
            }

            // 매입가치 점선 (더 굵게)
            val dashLength = 10.dp.toPx()
            val gapLength = 5.dp.toPx()
            var currentX = startX

            while (currentX < startX + width) {
                val endX = minOf(currentX + dashLength, startX + width)
                drawLine(
                    color = Color(0xFF6B7280),
                    start = Offset(currentX, purchaseY),
                    end = Offset(endX, purchaseY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                currentX = endX + gapLength
            }

            // 매입가치 표시점 (원)
            drawCircle(
                color = Color(0xFF6B7280),
                radius = 5.dp.toPx(),
                center = Offset(startX + width - 40.dp.toPx(), purchaseY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(startX + width - 40.dp.toPx(), purchaseY)
            )

            // 매입가치 라벨 (배경과 함께)
            drawContext.canvas.nativeCanvas.apply {
                val labelText = "매입가치"
                val paint = android.graphics.Paint().apply {
                    color = Color.White.toArgb()
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                // 라벨 배경
                val textBounds = android.graphics.Rect()
                paint.getTextBounds(labelText, 0, labelText.length, textBounds)
                val labelX = startX + width - 90.dp.toPx()
                val labelY = purchaseY - 15.dp.toPx()

                // 배경 사각형
                drawRoundRect(
                    labelX - textBounds.width()/2 - 8.dp.toPx(),
                    labelY - textBounds.height() - 4.dp.toPx(),
                    labelX + textBounds.width()/2 + 8.dp.toPx(),
                    labelY + 4.dp.toPx(),
                    8.dp.toPx(),
                    8.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = Color(0xFF6B7280).toArgb()
                        isAntiAlias = true
                    }
                )

                // 텍스트
                drawText(
                    labelText,
                    labelX,
                    labelY,
                    paint
                )
            }
        }

        // Gradient background
        val gradientPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, startY + height)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, startY + height)
                close()
            }
        }

        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = chartColors.gradient
            )
        )

        // Main line with enhanced styling
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
        }

        // Shadow effect
        drawPath(
            path = linePath,
            color = chartColors.primary.copy(alpha = 0.3f),
            style = Stroke(
                width = 5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Main line
        drawPath(
            path = linePath,
            color = chartColors.primary,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Enhanced data points
        points.forEach { point ->
            // Outer circle (shadow)
            drawCircle(
                color = chartColors.primary.copy(alpha = 0.3f),
                radius = 8.dp.toPx(),
                center = point
            )
            // White border
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = point
            )
            // Inner circle
            drawCircle(
                color = chartColors.primary,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun AssetPriceChart(
    priceHistory: List<StockHistoryItem>,
    shares: Double = 1.0,
    asset: Asset? = null,
    purchaseValue: Float? = null, // 매입가치 추가
    exchangeRate: ExchangeRate
) {
    val data = priceHistory.associate { historyItem ->
        val valueInMarketCurrency = historyItem.stockPrice * shares
        val valueInKRW = when (asset?.market?.currency) {
            Currency.KRW -> valueInMarketCurrency
            Currency.USD -> exchangeRate.usdToKrw(valueInMarketCurrency)
            null -> valueInMarketCurrency
        }
        historyItem.stockDate to valueInKRW.toFloat()
    }

    PortfolioLineChart(
        data = data,
        isPortfolioChart = false,
        purchaseValue = purchaseValue
    )
}