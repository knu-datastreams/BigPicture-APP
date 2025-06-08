package com.bigPicture.businessreportgenerator.presentation.features.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bigPicture.businessreportgenerator.data.domain.Asset
import com.bigPicture.businessreportgenerator.data.domain.AssetType
import com.bigPicture.businessreportgenerator.data.domain.Currency
import com.bigPicture.businessreportgenerator.data.domain.ExchangeRate
import com.bigPicture.businessreportgenerator.data.local.StockViewModel
import com.bigPicture.businessreportgenerator.presentation.common.PieChart
import com.bigPicture.businessreportgenerator.presentation.common.PieChartData
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

// InvestmentTip 정의
data class InvestmentTip(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val gradient: Brush
)

// InvestmentTips 정의
val InvestmentTips = listOf(
    InvestmentTip(
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
    ),
    InvestmentTip(
        title = "장기 투자 전략",
        description = "시간의 힘을 활용한 장기 투자로 복리 효과를 누려보세요.",
        icon = Icons.Rounded.Edit,
        iconColor = Color.White,
        gradient = Brush.linearGradient(
            listOf(
                Color(0xFF10B981),
                Color(0xFF059669)
            )
        )
    ),
    InvestmentTip(
        title = "정기적인 모니터링",
        description = "포트폴리오를 정기적으로 점검하고 필요시 리밸런싱하세요.",
        icon = Icons.Rounded.Star,
        iconColor = Color.White,
        gradient = Brush.linearGradient(
            listOf(
                Color(0xFF8B5CF6),
                Color(0xFF7C3AED)
            )
        )
    )
)

@Composable
fun PortfolioScreen(
    modifier: Modifier = Modifier
) {
    val portfolioViewModel: PortfolioViewModel = koinViewModel()
    val portfolioState by portfolioViewModel.state.collectAsState()
    val stockViewModel: StockViewModel = koinViewModel()
    val scrollState = rememberScrollState()

    // 분석 화면 표시 상태 추가
    var showAnalysisScreen by remember { mutableStateOf(false) }

    // 더욱 아름다운 그라데이션 컬러 팔레트
    val gorgeousColors = listOf(
        Color(0xFF667eea), // 매혹적인 보라-파랑
        Color(0xFFf093fb), // 드림핑크
        Color(0xFF4facfe), // 스카이블루
        Color(0xFF43e97b), // 민트그린
        Color(0xFFfad0c4), // 피치
        Color(0xFFa8edea), // 아쿠아
        Color(0xFFfed6e3), // 로즈
        Color(0xFFd299c2)  // 라벤더
    )

    val exchangeRate = remember { ExchangeRate(1300.0) }

    val pieChartData = portfolioState.assets.mapIndexed { index, asset ->
        PieChartData(
            value = (asset.getCurrentValueInKRW(exchangeRate)?.toFloat() ?: asset.purchasePrice.toFloat()),
            color = gorgeousColors[index % gorgeousColors.size],
            label = asset.name
        )
    }

    // 분석 화면이 표시되면 분석 화면을 보여주고, 아니면 기존 포트폴리오 화면 표시
    if (showAnalysisScreen) {
        PortfolioAnalysisScreen(
            assets = portfolioState.assets,
            onBackPressed = { showAnalysisScreen = false }, // 뒤로가기 시 원래 화면으로
            modifier = modifier
        )
    } else {
        // 기존 포트폴리오 화면
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF7FAFF),
                            Color(0xFFFFFFFF),
                            Color(0xFFF0F9FF)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 글래스모피즘 네비게이션 바
                GlassmorphismNavigationBar(
                    isRefreshing = portfolioState.isLoadingPrices,
                    onRefresh = { portfolioViewModel.refreshStockPrices() },
                    hasStocks = portfolioState.assets.any { it.type == AssetType.STOCK }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 32.dp)
                ) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(1000, easing = FastOutSlowInEasing)) +
                                slideInVertically(tween(1000, easing = LinearOutSlowInEasing)) { it / 4 }
                    ) {
                        if (portfolioState.assets.isEmpty()) {
                            EmptyState(
                                onAddClick = { portfolioViewModel.showAddAssetDialog() },
                                onShowSampleClick = { portfolioViewModel.showSamplePortfolioDialog() }
                            )
                        } else {
                            MesmerizingPortfolioContent(
                                portfolioState = portfolioState,
                                pieChartData = pieChartData,
                                exchangeRate = exchangeRate,
                                onAddClick = { portfolioViewModel.showAddAssetDialog() },
                                onAnalysisClick = { showAnalysisScreen = true } // 분석 버튼 클릭 시 분석 화면 표시
                            )
                        }
                    }
                }

                // 다이얼로그들
                if (portfolioState.isAddAssetDialogVisible) {
                    AddAssetDialog(
                        onDismiss = { portfolioViewModel.hideAddAssetDialog() },
                        onAddAsset = {
                            portfolioViewModel.addAsset(it)
                            if (it.type == AssetType.STOCK) {
                                val market = it.market?.name
                                val stockType = if (market == "KOSPI" || market == "KOSDAQ") "korea" else "us"
                                stockViewModel.registerStock(stockType, it.name)
                            }
                        },
                        currentExchangeRate = exchangeRate
                    )
                }

                if (portfolioState.isSamplePortfolioDialogVisible) {
                    SamplePortfolioDialog(
                        sampleAssets = portfolioState.sampleAssets,
                        onDismiss = { portfolioViewModel.hideSamplePortfolioDialog() },
                        onLoad = {
                            portfolioViewModel.loadSamplePortfolio()
                            portfolioViewModel.hideSamplePortfolioDialog()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GlassmorphismNavigationBar(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    hasStocks: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.7f),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Portfolio",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A2E),
                letterSpacing = (-1.2).sp
            )

            if (hasStocks) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667eea).copy(alpha = 0.2f),
                                    Color(0xFF764ba2).copy(alpha = 0.2f)
                                )
                            )
                        )
                        .clickable { if (!isRefreshing) onRefresh() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF667eea)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침",
                            tint = Color(0xFF667eea),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MesmerizingPortfolioContent(
    portfolioState: ModernPortfolioState,
    pieChartData: List<PieChartData>,
    exchangeRate: ExchangeRate,
    onAddClick: () -> Unit,
    onAnalysisClick: () -> Unit // 분석 클릭 콜백 추가
) {
    val animatedTotalValue by animateFloatAsState(
        targetValue = portfolioState.totalPortfolioValue.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "totalValue"
    )

    val animatedReturnValue by animateFloatAsState(
        targetValue = portfolioState.totalReturnValue.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "returnValue"
    )

    val formatter = NumberFormat.getCurrencyInstance(Locale.KOREA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 매혹적인 메인 자산 카드
        EnchantingMainAssetCard(
            totalValue = animatedTotalValue.toDouble(),
            returnValue = animatedReturnValue.toDouble(),
            returnPercentage = portfolioState.totalReturnPercentage,
            formatter = formatter,
            onAddClick = onAddClick,
            onAnalysisClick = onAnalysisClick // 분석 클릭 콜백 전달
        )

        // 드림라이크 차트 카드
        DreamlikeChartCard(
            pieChartData = pieChartData,
            assets = portfolioState.assets
        )

        // 럭셔리 자산 목록
        LuxuryAssetList(
            assets = portfolioState.assets,
            totalValue = portfolioState.totalPortfolioValue,
            pieChartData = pieChartData,
            exchangeRate = exchangeRate,
            isLoadingPrices = portfolioState.isLoadingPrices,
            onAddClick = onAddClick
        )
    }
}


@Composable
fun EnchantingMainAssetCard(
    totalValue: Double,
    returnValue: Double,
    returnPercentage: Double,
    formatter: NumberFormat,
    onAddClick: () -> Unit,
    onAnalysisClick: () -> Unit // 분석 클릭 콜백 추가
) {
    val isProfit = returnValue >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(32.dp),
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
                                Color(0xFF764ba2),
                                Color(0xFF667eea)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFf093fb),
                                Color(0xFFf5576c),
                                Color(0xFFf093fb)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    },
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(32.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "총 자산",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = formatter.format(totalValue),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1.5).sp
                        )

                        Spacer(Modifier.height(20.dp))

                        // 매혹적인 수익률 표시
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isProfit) {
                                        Icons.Outlined.KeyboardArrowUp
                                    } else {
                                        Icons.Outlined.KeyboardArrowDown
                                    },
                                    contentDescription = "수익률",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${if (isProfit) "+" else ""}${formatter.format(returnValue)}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp
                                )
                                Text(
                                    text = "${if (isProfit) "+" else ""}${String.format("%.2f", returnPercentage)}%",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // 매혹적인 플로팅 버튼
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "추가",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // 매혹적인 퀵 액션들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnchantingQuickAction(
                        icon = Icons.Rounded.Edit,
                        label = "분석",
                        onClick = onAnalysisClick // 여기에 분석 클릭 콜백 연결!
                    )
                    EnchantingQuickAction(
                        icon = Icons.Rounded.Star,
                        label = "성과",
                        onClick = {}
                    )
                    EnchantingQuickAction(
                        icon = Icons.Filled.Add,
                        label = "추가",
                        onClick = onAddClick
                    )
                }
            }
        }
    }
}


@Composable
fun EnchantingQuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@Composable
fun DreamlikeChartCard(
    pieChartData: List<PieChartData>,
    assets: List<Asset>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "자산 구성",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A2E),
                    letterSpacing = (-0.6).sp
                )
                TextButton(
                    onClick = {},
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF667eea)
                    )
                ) {
                    Text(
                        text = "더보기",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                PieChart(data = pieChartData, assets = assets)
            }
        }
    }
}

@Composable
fun LuxuryAssetList(
    assets: List<Asset>,
    totalValue: Double,
    pieChartData: List<PieChartData>,
    exchangeRate: ExchangeRate,
    isLoadingPrices: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "보유 종목",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A2E),
                    letterSpacing = (-0.6).sp
                )

                if (isLoadingPrices) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF667eea)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "업데이트 중",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            assets.forEachIndexed { index, asset ->
                AssetItem(
                    asset = asset,
                    totalValue = totalValue,
                    color = pieChartData[index].color,
                    exchangeRate = exchangeRate
                )
                if (index < assets.size - 1) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(
                        color = Color(0xFFF5F5F7),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 매혹적인 추가 버튼
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = Color(0xFF667eea).copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "추가",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "종목 추가",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun AssetItem(
    asset: Asset,
    totalValue: Double,
    color: Color,
    exchangeRate: ExchangeRate
) {
    val currentValueInKRW = asset.getCurrentValueInKRW(exchangeRate) ?: asset.purchasePrice
    val proportion = if (totalValue > 0) (currentValueInKRW / totalValue) * 100 else 0.0
    val portfolioFormatter = NumberFormat.getCurrencyInstance(Locale.KOREA)

    val returnPercentage = asset.getReturnPercentage()
    val returnAmountInKRW = asset.getReturnAmountInKRW(exchangeRate)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 매혹적인 아이콘
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            color.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = asset.name.take(2),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color,
                letterSpacing = (-0.4).sp
            )
        }

        Spacer(Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when(asset.type) {
                        AssetType.STOCK -> "주식"
                        AssetType.ETF -> "ETF"
                        AssetType.CRYPTO -> "암호화폐"
                        else -> "기타"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Medium
                )
                asset.ticker?.let { ticker ->
                    Text(
                        text = " • $ticker",
                        fontSize = 14.sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                asset.market?.let { market ->
                    Text(
                        text = " • ${market.displayName}",
                        fontSize = 14.sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = portfolioFormatter.format(currentValueInKRW),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(2.dp))

            if (returnPercentage != null && returnAmountInKRW != null) {
                val isProfit = returnAmountInKRW >= 0
                Text(
                    text = "${if (isProfit) "+" else ""}${String.format("%.2f", returnPercentage)}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) Color(0xFF34C759) else Color(0xFFFF3B30)
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${proportion.toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    onAddClick: () -> Unit,
    onShowSampleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // 매혹적인 일러스트
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF667eea).copy(alpha = 0.2f),
                            Color(0xFF764ba2).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Face,
                contentDescription = "포트폴리오",
                tint = Color(0xFF667eea),
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "투자를 시작해보세요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF1A1A2E),
            textAlign = TextAlign.Center,
            letterSpacing = (-0.8).sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "종목을 추가하여 포트폴리오를 구성하고\n실시간 수익률을 확인해보세요",
            fontSize = 16.sp,
            color = Color(0xFF8E8E93),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            letterSpacing = (-0.2).sp
        )

        Spacer(modifier = Modifier.height(50.dp))

        // 매혹적인 메인 버튼
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 15.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0xFF667eea).copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "첫 종목 추가하기",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.3).sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 매혹적인 보조 버튼
        OutlinedButton(
            onClick = onShowSampleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFF667eea).copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF667eea)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "샘플",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "샘플 포트폴리오 보기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }
        }
    }
}


@Composable
fun CurrencyAwareAssetListItem(
    asset: Asset,
    totalValue: Double,
    color: Color,
    exchangeRate: ExchangeRate? = ExchangeRate(1300.0) // 현재 환율
) {
    val currentValueInKRW = asset.getCurrentValueInKRW(exchangeRate) ?: asset.purchasePrice
    val proportion = if (totalValue > 0) (currentValueInKRW / totalValue) * 100 else 0.0
    val market = asset.market

    // 포트폴리오 전체는 원화로 표시
    val portfolioFormatter = NumberFormat.getCurrencyInstance(Locale.KOREA)

    // 수익률 관련 계산
    val returnPercentage = asset.getReturnPercentage()
    val returnAmountInKRW = asset.getReturnAmountInKRW(exchangeRate)
    val returnAmountInMarketCurrency = asset.getReturnAmountInMarketCurrency()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 현대적인 아이콘 디자인
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = asset.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when(asset.type) {
                                AssetType.STOCK -> "주식"
                                AssetType.ETF -> "ETF"
                                AssetType.CRYPTO -> "암호화폐"
                                else -> "기타"
                            },
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )

                        // 티커와 거래소 표시
                        asset.ticker?.let { ticker ->
                            Text(
                                text = " • $ticker",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        market?.let { marketInfo ->
                            Text(
                                text = " • ${marketInfo.displayName}",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    // 현재 시장가치 (원화로 환산된 값)
                    Text(
                        text = portfolioFormatter.format(currentValueInKRW),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )

                    // 수익률 표시 (주식이고 현재가가 있는 경우)
                    if (asset.type == AssetType.STOCK && returnPercentage != null && returnAmountInKRW != null) {
                        val isProfit = returnAmountInKRW >= 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                imageVector = if (isProfit) {
                                    Icons.Outlined.KeyboardArrowUp
                                } else {
                                    Icons.Outlined.KeyboardArrowDown
                                },
                                contentDescription = "수익률",
                                tint = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${if (isProfit) "+" else ""}${String.format("%.2f", returnPercentage)}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${proportion.toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                    }
                }
            }

            // 주식인 경우 상세 정보 표시
            if (asset.type == AssetType.STOCK) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 구매 정보 (해당 시장 통화)
                    Column {
                        Text(
                            text = "매입가",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        asset.purchasePricePerShare?.let { purchasePrice ->
                            Text(
                                text = market?.currency?.formatAmount(purchasePrice) ?: "₩${String.format("%.0f", purchasePrice)}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 현재가 (해당 시장 통화)
                    if (asset.currentPrice != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "현재가",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = market?.currency?.formatAmount(asset.currentPrice) ?: "₩${String.format("%.0f", asset.currentPrice)}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 보유 수량 표시
                asset.shares?.let { shareCount ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "보유 수량: ${String.format("%.1f", shareCount)}주",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // 수익금 표시 (원화와 해당 통화 둘 다)
                if (returnAmountInKRW != null && returnAmountInMarketCurrency != null && market != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val isProfit = returnAmountInKRW >= 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 해당 시장 통화로 수익금
                        Text(
                            text = "${if (isProfit) "+" else ""}${market.currency.formatAmount(returnAmountInMarketCurrency)}",
                            fontSize = 11.sp,
                            color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )

                        // 원화로 수익금 (USD 시장인 경우만)
                        if (market.currency == Currency.USD) {
                            Text(
                                text = "${if (isProfit) "+" else ""}${Currency.KRW.formatAmount(returnAmountInKRW)}",
                                fontSize = 11.sp,
                                color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 마지막 업데이트 시간
                asset.lastUpdated?.let { lastUpdated ->
                    val timeDiff = System.currentTimeMillis() - lastUpdated
                    val minutesAgo = (timeDiff / (1000 * 60)).toInt()
                    if (minutesAgo < 60) {
                        Text(
                            text = "${minutesAgo}분 전 업데이트",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernQuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModernEmptyPortfolioContent(
    onAddClick: () -> Unit,
    onShowSampleClick: () -> Unit
) {
    // 랜덤 투자 팁 선택
    var currentTip by remember { mutableStateOf(InvestmentTips.random()) }

    LaunchedEffect(Unit) {
        // 앱 시작시 랜덤 팁 선택
        currentTip = InvestmentTips[Random.nextInt(InvestmentTips.size)]
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 현대적인 투자 팁 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f)
                ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = currentTip.gradient,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentTip.icon,
                            contentDescription = currentTip.title,
                            tint = currentTip.iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTip.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentTip.description,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // 현대적인 시작하기 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f)
                ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 현대적인 아이콘 디자인
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.1f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.1f)
                                )
                            )
                        )
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Face,
                        contentDescription = "포트폴리오",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "투자를 시작해보세요",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "종목을 추가하여 포트폴리오를 구성하고\n실시간 수익률을 확인해보세요",
                    fontSize = 16.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // 현대적인 CTA 버튼
                Button(
                    onClick = onAddClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color(0xFF6366F1).copy(alpha = 0.25f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "첫 종목 추가하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onShowSampleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6366F1)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "샘플",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "샘플 포트폴리오 보기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamplePortfolioDialog(
    sampleAssets: List<Asset>,
    onDismiss: () -> Unit,
    onLoad: () -> Unit
) {
    val modernColors = listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFF06B6D4),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFFEC4899),
        Color(0xFF84CC16)
    )

    val pieData = sampleAssets.mapIndexed { idx, asset ->
        PieChartData(
            value = asset.purchasePrice.toFloat(),
            color = modernColors[idx % modernColors.size],
            label = asset.name
        )
    }

    val totalSampleValue = sampleAssets.sumOf { it.purchasePrice }
    val formatter = NumberFormat.getCurrencyInstance(Locale.KOREA)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400)) +
                        slideInVertically(animationSpec = tween(400)) { it / 3 },
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .heightIn(max = 680.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        // 현대적인 헤더
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0xFF667eea),
                                            Color(0xFF764ba2)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = "샘플",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "샘플 포트폴리오",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "글로벌 테크 포트폴리오",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "닫기",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // 총 자산 정보
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8FAFC)
                                )
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
                                            text = "총 투자금액 (원화 환산)",
                                            fontSize = 14.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatter.format(totalSampleValue),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(0xFF6366F1),
                                                        Color(0xFF8B5CF6)
                                                    )
                                                )
                                            )
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "분석",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 파이 차트
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "자산 구성",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    ) {
                                        PieChart(
                                            data = pieData,
                                            assets = sampleAssets
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 종목 리스트
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "보유 종목",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(sampleAssets) { asset ->
                                            CurrencyAwareSampleAssetItem(
                                                asset = asset,
                                                totalValue = totalSampleValue,
                                                color = modernColors[sampleAssets.indexOf(asset) % modernColors.size]
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 설명 카드
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF0F9FF)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = "정보",
                                        tint = Color(0xFF0EA5E9),
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = "글로벌 테크 기업 중심의 성장형 포트폴리오입니다.\n실시간 가격 업데이트와 통화별 수익률 확인이 가능합니다.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF0369A1),
                                        lineHeight = 20.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 액션 버튼들
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF64748B)
                                    )
                                ) {
                                    Text(
                                        text = "취소",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    onClick = onLoad,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = RoundedCornerShape(16.dp),
                                            spotColor = Color(0xFF6366F1).copy(alpha = 0.25f)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6366F1),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "포트폴리오 적용",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyAwareSampleAssetItem(
    asset: Asset,
    totalValue: Double,
    color: Color
) {
    val proportion = if (totalValue > 0) (asset.purchasePrice / totalValue) * 100 else 0.0
    val market = asset.details["market"]
    val portfolioFormatter = NumberFormat.getCurrencyInstance(Locale.KOREA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 현대적인 아이콘 디자인
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when(asset.type) {
                        AssetType.STOCK -> "주식"
                        AssetType.ETF -> "ETF"
                        AssetType.CRYPTO -> "암호화폐"
                        else -> "기타"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )

                asset.ticker?.let { ticker ->
                    Text(
                        text = " • $ticker",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }

                market?.let { marketName ->
                    Text(
                        text = " • $marketName",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = portfolioFormatter.format(asset.purchasePrice),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${proportion.toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }
    }
}