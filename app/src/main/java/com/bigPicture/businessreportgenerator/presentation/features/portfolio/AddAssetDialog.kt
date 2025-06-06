package com.bigPicture.businessreportgenerator.presentation.features.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bigPicture.businessreportgenerator.data.domain.Asset
import com.bigPicture.businessreportgenerator.data.domain.AssetType
import com.bigPicture.businessreportgenerator.data.domain.Currency
import com.bigPicture.businessreportgenerator.data.domain.ExchangeRate
import com.bigPicture.businessreportgenerator.data.domain.Market
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

data class AssetTypeOption(
    val type: AssetType,
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

val AssetTypes = listOf(
    AssetTypeOption(
        type = AssetType.STOCK,
        displayName = "주식",
        icon = Icons.Rounded.AddCircle,
        color = Color(0xFF6366F1),
        description = "개별 기업 주식"
    ),
    AssetTypeOption(
        type = AssetType.ETF,
        displayName = "ETF",
        icon = Icons.Rounded.AccountBox,
        color = Color(0xFF8B5CF6),
        description = "상장지수펀드"
    ),
    AssetTypeOption(
        type = AssetType.CRYPTO,
        displayName = "암호화폐",
        icon = Icons.Rounded.Star,
        color = Color(0xFFF59E0B),
        description = "디지털 자산"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetDialog(
    onDismiss: () -> Unit,
    onAddAsset: (Asset) -> Unit,
    currentExchangeRate: ExchangeRate?
) {
    val portfolioViewModel: PortfolioViewModel = koinViewModel()
    val portfolioState by portfolioViewModel.state.collectAsState()

    var selectedMarket by remember { mutableStateOf<Market?>(null) }
    var showMarketSelection by remember { mutableStateOf(true) }
    var assetName by remember { mutableStateOf("") }
    var ticker by remember { mutableStateOf("") }
    var purchasePricePerShare by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf("") }
    var lastValidatedTicker by remember { mutableStateOf("") }

    // 실시간 환율 정보 사용
    val realTimeExchangeRate = portfolioState.exchangeRateInfo.usdToKrw
    val exchangeRateLastUpdated = portfolioState.exchangeRateInfo.lastUpdated

    // 티커 검증 로직
    LaunchedEffect(ticker) {
        if (ticker.isNotEmpty() && ticker != lastValidatedTicker && ticker.length >= 2) {
            lastValidatedTicker = ticker
            portfolioViewModel.validateTicker(ticker)
        } else if (ticker.isEmpty()) {
            portfolioViewModel.clearTickerValidation()
        }
    }

    // 조회된 티커가 있을 때 종목명 자동 입력
    LaunchedEffect(portfolioState.tickerValidationResult) {
        portfolioState.tickerValidationResult?.let { result ->
            if (result.isValid && result.companyName != null && assetName.isEmpty()) {
                assetName = result.companyName
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(600)) + scaleIn(
                    tween(600, easing = FastOutSlowInEasing),
                    initialScale = 0.8f
                ),
                exit = fadeOut(tween(400)) + scaleOut(tween(400))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.85f) // 최대 높이 제한 추가
                        .shadow(
                            elevation = 40.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color.Black.copy(alpha = 0.2f),
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White
                ) {
                    Column {
                        // 고정 헤더
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "종목 추가",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1A2E),
                                letterSpacing = (-0.8).sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F5F7))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "닫기",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 스크롤 가능한 컨텐츠
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // 남은 공간 차지
                                .verticalScroll(rememberScrollState()) // 스크롤 추가
                                .padding(horizontal = 32.dp)
                                .padding(bottom = 32.dp)
                        ) {
                            if (showMarketSelection) {
                                // 매혹적인 시장 선택
                                Text(
                                    text = "거래소를 선택하세요",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A2E),
                                    letterSpacing = (-0.4).sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Market.values().forEach { market ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                selectedMarket = market
                                                showMarketSelection = false
                                            }
                                            .shadow(
                                                elevation = 8.dp,
                                                shape = RoundedCornerShape(20.dp),
                                                ambientColor = Color.Black.copy(alpha = 0.04f)
                                            ),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(0.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = market.displayName,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1A1A2E),
                                                    letterSpacing = (-0.3).sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${market.currency.symbol} ${market.currency.code}",
                                                    fontSize = 15.sp,
                                                    color = Color(0xFF8E8E93),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                Color(0xFF667eea),
                                                                Color(0xFF764ba2)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "선택",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp)) // 하단 여백

                            } else {
                                // 실시간 환율 정보 표시 (USD 마켓 선택시)
                                selectedMarket?.let { market ->
                                    if (market.currency == Currency.USD) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 20.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFFF0F9FF)
                                            ),
                                            border = BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF0EA5E9).copy(alpha = 0.2f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.ShoppingCart,
                                                            contentDescription = "환율",
                                                            tint = Color(0xFF0EA5E9),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Column {
                                                        Text(
                                                            text = "현재 환율",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF0369A1),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        if (portfolioState.exchangeRateInfo.isLoading) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                CircularProgressIndicator(
                                                                    modifier = Modifier.size(12.dp),
                                                                    strokeWidth = 2.dp,
                                                                    color = Color(0xFF0EA5E9)
                                                                )
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "업데이트 중...",
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF0EA5E9)
                                                                )
                                                            }
                                                        } else {
                                                            Text(
                                                                text = "1 USD = ${String.format("%.2f", realTimeExchangeRate)} KRW",
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF0369A1)
                                                            )
                                                        }
                                                    }
                                                }

                                                Column(
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    IconButton(
                                                        onClick = { portfolioViewModel.refreshExchangeRate() },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Refresh,
                                                            contentDescription = "환율 새로고침",
                                                            tint = Color(0xFF0EA5E9),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }

                                                    exchangeRateLastUpdated?.let { lastUpdated ->
                                                        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                                                        Text(
                                                            text = formatter.format(lastUpdated),
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF0369A1),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 매혹적인 종목 정보 입력
                                selectedMarket?.let { market ->
                                    // 선택된 시장 표시
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF667eea).copy(alpha = 0.1f)
                                        ),
                                        border = BorderStroke(2.dp, Color(0xFF667eea).copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "선택된 거래소",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF667eea).copy(alpha = 0.8f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${market.displayName} (${market.currency.symbol})",
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF667eea)
                                                )
                                            }
                                            TextButton(
                                                onClick = {
                                                    showMarketSelection = true
                                                    assetName = ""
                                                    ticker = ""
                                                    purchasePricePerShare = ""
                                                    shares = ""
                                                }
                                            ) {
                                                Text(
                                                    "변경",
                                                    color = Color(0xFF667eea),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(28.dp))

                                    TextField(
                                        value = assetName,
                                        onValueChange = { assetName = it },
                                        label = "종목명",
                                        placeholder = "예: 삼성전자, 애플"
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // 티커 입력 필드 부분
                                    TextField(
                                        value = ticker,
                                        onValueChange = {
                                            val newTicker = it.uppercase()
                                            ticker = newTicker
                                            if (newTicker != lastValidatedTicker) {
                                                portfolioViewModel.clearTickerValidation()
                                            }
                                        },
                                        label = "티커 심볼",
                                        placeholder = when (selectedMarket) {
                                            Market.KOSPI, Market.KOSDAQ -> "예: 005930"
                                            Market.NYSE, Market.NASDAQ -> "예: AAPL"
                                            else -> "티커를 입력하세요"
                                        },
                                        isLoading = portfolioState.isValidatingTicker
                                    )

                                    // 티커 검증 결과 표시
                                    portfolioState.tickerValidationResult?.let { result ->
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (result.isValid) {
                                                    Color(0xFF34C759).copy(alpha = 0.1f)
                                                } else {
                                                    Color(0xFFFF3B30).copy(alpha = 0.1f)
                                                }
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (result.isValid) {
                                                    Color(0xFF34C759).copy(alpha = 0.3f)
                                                } else {
                                                    Color(0xFFFF3B30).copy(alpha = 0.3f)
                                                }
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (result.isValid) {
                                                        Icons.Default.CheckCircle
                                                    } else {
                                                        Icons.Default.Close
                                                    },
                                                    contentDescription = if (result.isValid) "성공" else "실패",
                                                    tint = if (result.isValid) {
                                                        Color(0xFF34C759)
                                                    } else {
                                                        Color(0xFFFF3B30)
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                )

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    if (result.isValid) {
                                                        Text(
                                                            text = if (result.actualTicker != null && result.actualTicker != ticker) {
                                                                "${result.actualTicker} 조회되었습니다"
                                                            } else {
                                                                "$ticker 조회되었습니다"
                                                            },
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF34C759)
                                                        )
                                                        result.companyName?.let { name ->
                                                            Text(
                                                                text = name,
                                                                fontSize = 13.sp,
                                                                color = Color(0xFF34C759).copy(alpha = 0.8f),
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = result.errorMessage ?: "유효하지 않은 티커입니다",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color(0xFFFF3B30)
                                                        )
                                                    }
                                                }

                                                // 조회된 티커가 입력한 것과 다르면 적용 버튼 표시
                                                if (result.isValid && result.actualTicker != null && result.actualTicker != ticker) {
                                                    TextButton(
                                                        onClick = {
                                                            ticker = result.actualTicker
                                                            lastValidatedTicker = result.actualTicker
                                                        },
                                                        colors = ButtonDefaults.textButtonColors(
                                                            contentColor = Color(0xFF34C759)
                                                        )
                                                    ) {
                                                        Text(
                                                            text = "적용",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    TextField(
                                        value = purchasePricePerShare,
                                        onValueChange = { purchasePricePerShare = it },
                                        label = "매입 주가 (${market.currency.symbol})",
                                        placeholder = when (market.currency) {
                                            Currency.KRW -> "예: 50,000"
                                            Currency.USD -> "예: 150.50"
                                        },
                                        keyboardType = KeyboardType.Number
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    TextField(
                                        value = shares,
                                        onValueChange = { shares = it },
                                        label = "보유 주식 수",
                                        placeholder = "예: 10",
                                        keyboardType = KeyboardType.Number
                                    )

                                    // 매혹적인 미리보기
                                    val pricePerShare = purchasePricePerShare.toDoubleOrNull()
                                    val shareCount = shares.toDoubleOrNull()
                                    if (pricePerShare != null && shareCount != null) {
                                        val totalInMarketCurrency = pricePerShare * shareCount

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF34C759).copy(alpha = 0.1f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp)
                                            ) {
                                                Text(
                                                    text = "총 투자금액",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF34C759).copy(alpha = 0.8f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = market.currency.formatAmount(totalInMarketCurrency),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF34C759)
                                                )

                                                // USD인 경우 실시간 환율로 원화 환산 표시
                                                if (market.currency == Currency.USD && !portfolioState.exchangeRateInfo.isLoading) {
                                                    val totalInKRW = realTimeExchangeRate * totalInMarketCurrency
                                                    Text(
                                                        text = "원화 환산: ${Currency.KRW.formatAmount(totalInKRW)}",
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF34C759).copy(alpha = 0.8f),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "환율: 1 USD = ${String.format("%.2f", realTimeExchangeRate)} KRW",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF34C759).copy(alpha = 0.7f),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(32.dp))

                                    val canAdd = assetName.isNotEmpty() &&
                                            ticker.isNotEmpty() &&
                                            portfolioState.tickerValidationResult?.isValid == true && // 검증 결과가 유효할 때만
                                            pricePerShare != null &&
                                            shareCount != null

                                    Button(
                                        onClick = {
                                            val totalInMarketCurrency = pricePerShare!! * shareCount!!

                                            // 실시간 환율 사용
                                            val totalInKRW = when (market.currency) {
                                                Currency.KRW -> totalInMarketCurrency
                                                Currency.USD -> realTimeExchangeRate * totalInMarketCurrency
                                            }

                                            val details = mutableMapOf<String, String>()
                                            details["market"] = market.name
                                            details["purchasePricePerShare"] = pricePerShare.toString()
                                            details["shares"] = shareCount.toString()

                                            val asset = Asset(
                                                name = assetName,
                                                type = AssetType.STOCK,
                                                purchasePrice = totalInKRW,
                                                ticker = ticker,
                                                purchaseDate = System.currentTimeMillis(),
                                                details = details
                                            )
                                            onAddAsset(asset)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .shadow(
                                                elevation = if (canAdd) 12.dp else 0.dp,
                                                shape = RoundedCornerShape(18.dp),
                                                spotColor = Color(0xFF667eea).copy(alpha = 0.4f)
                                            ),
                                        shape = RoundedCornerShape(18.dp),
                                        enabled = canAdd,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            disabledContainerColor = Color(0xFFF5F5F7)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    if (canAdd) {
                                                        Brush.horizontalGradient(
                                                            colors = listOf(
                                                                Color(0xFF667eea),
                                                                Color(0xFF764ba2)
                                                            )
                                                        )
                                                    } else {
                                                        Brush.horizontalGradient(
                                                            colors = listOf(
                                                                Color(0xFFE5E5EA),
                                                                Color(0xFFE5E5EA)
                                                            )
                                                        )
                                                    },
                                                    shape = RoundedCornerShape(18.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "종목 추가",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (canAdd) Color.White else Color(0xFF8E8E93),
                                                letterSpacing = (-0.3).sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(32.dp)) // 하단 여백
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isLoading: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E),
            letterSpacing = (-0.3).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = Color(0xFFAEAEB2),
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF667eea),
                unfocusedBorderColor = Color(0xFFE5E5EA),
                cursorColor = Color(0xFF667eea),
                containerColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF1A1A2E),
                fontWeight = FontWeight.Medium
            ),
            trailingIcon = if (isLoading) {
                {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF667eea)
                    )
                }
            } else null
        )
    }
}