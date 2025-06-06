package com.bigPicture.businessreportgenerator.presentation.features.analyst

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bigPicture.businessreportgenerator.data.domain.AnalystReport
import com.bigPicture.businessreportgenerator.data.domain.GraphData
import com.bigPicture.businessreportgenerator.data.domain.ReportSentiment
import com.bigPicture.businessreportgenerator.data.domain.getColor
import com.bigPicture.businessreportgenerator.data.domain.getDisplayName
import java.text.SimpleDateFormat
import java.util.Locale

// 컬러 팔레트 정의
object FinancialColors {
    val Primary = Color(0xFF0066FF)
    val Secondary = Color(0xFF00D4AA)
    val Background = Color(0xFFF8FAFC)
    val Surface = Color.White
    val SurfaceVariant = Color(0xFFF1F5F9)
    val OnSurface = Color(0xFF1E293B)
    val OnSurfaceVariant = Color(0xFF64748B)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Neutral = Color(0xFF6B7280)

    // 그래프 색상
    val GraphPrimary = Color(0xFF3B82F6)
    val GraphSecondary = Color(0xFF8B5CF6)
    val GraphTertiary = Color(0xFF06B6D4)
    val GraphAccent = Color(0xFFF59E0B)
}

/**
 * 메인 애널리스트 화면 - 현대적 디자인
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnalystScreen(modifier: Modifier = Modifier) {
    var isFilterOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val viewModel: AnalystViewmodel = viewModel(
        factory = remember { AnalystViewModelFactory(context.applicationContext) }
    )

    val filter by viewModel.filterState.collectAsState()
    val selectedCategory = filter.category
    val selectedSentiment = filter.sentiment
    val selectedReport by viewModel.selectedReport.collectAsState()
    val reports by viewModel.filteredReports.collectAsState()
    val categories by viewModel.categoriesFlow.collectAsState()
    val sentiments = viewModel.sentiments

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FinancialColors.Background)
    ) {
        if (selectedReport == null) {
            // 메인 리스트 화면
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 헤더
                stickyHeader {
                    ModernTopBar()
                }

                // 필터 섹션
                stickyHeader {
                    ModernFilterSection(
                        isOpen = isFilterOpen,
                        onToggle = { isFilterOpen = !isFilterOpen },
                        categories = categories,
                        sentiments = sentiments,
                        selectedCategory = selectedCategory,
                        selectedSentiment = selectedSentiment,
                        onCategorySelected = { viewModel.setSelectedCategory(it) },
                        onSentimentSelected = { viewModel.setSelectedSentiment(it) }
                    )
                }

                // 보고서 리스트
                items(reports, key = { it.id }) { report ->
                    ModernReportCard(
                        report = report,
                        onClick = { viewModel.setSelectedReport(report) }
                    )
                }
            }
        } else {
            // 상세 화면
            ModernReportDetailScreen(
                report = selectedReport!!,
                onBackPressed = { viewModel.setSelectedReport(null) }
            )
        }
    }
}

@Composable
fun ModernTopBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FinancialColors.Surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI 투자 리포트",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinancialColors.OnSurface
                    )
                    Text(
                        text = "실시간 시장 분석 및 전망",
                        fontSize = 14.sp,
                        color = FinancialColors.OnSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            FinancialColors.Primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = FinancialColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernFilterSection(
    isOpen: Boolean,
    onToggle: () -> Unit,
    categories: List<String>,
    sentiments: List<ReportSentiment>,
    selectedCategory: String?,
    selectedSentiment: ReportSentiment?,
    onCategorySelected: (String?) -> Unit,
    onSentimentSelected: (ReportSentiment?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 필터 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccountBox,
                        contentDescription = null,
                        tint = FinancialColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "필터",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinancialColors.OnSurface
                    )
                }

                Icon(
                    imageVector = if (isOpen) Icons.Default.AccountBox else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = FinancialColors.OnSurfaceVariant
                )
            }

            if (isOpen) {
                Spacer(modifier = Modifier.height(16.dp))

                // 카테고리 필터
                ModernFilterChipGroup(
                    title = "카테고리",
                    items = categories,
                    selectedItem = selectedCategory,
                    onItemSelected = onCategorySelected,
                    itemToString = { it },
                    getItemColor = { FinancialColors.Primary }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 감정 필터
                ModernFilterChipGroup(
                    title = "시장 전망",
                    items = sentiments,
                    selectedItem = selectedSentiment,
                    onItemSelected = onSentimentSelected,
                    itemToString = { it.getDisplayName() },
                    getItemColor = { it.getColor() }
                )
            }
        }
    }
}

@Composable
fun <T> ModernFilterChipGroup(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    itemToString: (T) -> String,
    getItemColor: (T) -> Color
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = FinancialColors.OnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 전체 선택 칩
            FilterChip(
                selected = selectedItem == null,
                onClick = { onItemSelected(null) },
                label = {
                    Text(
                        "전체",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FinancialColors.Primary,
                    selectedLabelColor = Color.White,
                    containerColor = FinancialColors.SurfaceVariant,
                    labelColor = FinancialColors.OnSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedItem == null,
                    borderColor = if (selectedItem == null) FinancialColors.Primary else Color.Transparent
                )
            )

            // 개별 아이템 칩들
            items.forEach { item ->
                FilterChip(
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    label = {
                        Text(
                            itemToString(item),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getItemColor(item),
                        selectedLabelColor = Color.White,
                        containerColor = FinancialColors.SurfaceVariant,
                        labelColor = FinancialColors.OnSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedItem == item,
                        borderColor = if (selectedItem == item) getItemColor(item) else Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun ModernReportCard(
    report: AnalystReport,
    onClick: (AnalystReport) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick(report) },
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 헤더 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        val dateFormat = SimpleDateFormat("MM.dd", Locale.getDefault())
                        Text(
                            text = dateFormat.format(report.date),
                            fontSize = 12.sp,
                            color = FinancialColors.OnSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .background(
                                    FinancialColors.Primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = report.category,
                                fontSize = 11.sp,
                                color = FinancialColors.Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = report.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinancialColors.OnSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )
                }

                ModernSentimentTag(sentiment = report.sentiment)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 그래프 미리보기
            if (report.graphData.isNotEmpty()) {
                val firstGraph = report.graphData.first()
                ModernGraphPreview(
                    graphData = firstGraph,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FinancialColors.SurfaceVariant)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 요약
            Text(
                text = report.summary,
                fontSize = 14.sp,
                color = FinancialColors.OnSurfaceVariant,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 더보기 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "자세히 보기",
                    fontSize = 14.sp,
                    color = FinancialColors.Primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = FinancialColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ModernSentimentTag(sentiment: ReportSentiment) {
    val backgroundColor = sentiment.getColor().copy(alpha = 0.12f)
    val contentColor = sentiment.getColor()

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, contentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = sentiment.getDisplayName(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
fun ModernGraphPreview(
    graphData: GraphData,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (graphData.type) {
            "LINE_CHART" -> ModernLineChart(data = graphData.data)
            "BAR_CHART" -> ModernBarChart(data = graphData.data)
            "PIE_CHART" -> ModernPieChart(data = graphData.data)
            else -> {
                // 기본 플레이스홀더
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "차트 데이터 없음",
                        color = FinancialColors.OnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 그래프 제목
        if (graphData.title.isNotEmpty()) {
            Text(
                text = graphData.title,
                fontSize = 11.sp,
                color = FinancialColors.OnSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(
                        FinancialColors.Surface.copy(alpha = 0.9f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
fun ModernLineChart(data: Map<String, Float>) {
    if (data.isEmpty() || data.size < 2) return

    val values = data.values.toList()
    val max = values.maxOrNull() ?: 0f
    val min = values.minOrNull() ?: 0f
    val range = (max - min).takeIf { it != 0f } ?: 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width - 40.dp.toPx()
        val height = size.height - 40.dp.toPx()
        val startX = 20.dp.toPx()
        val startY = 20.dp.toPx()

        val stepX = width / (values.size - 1)

        // 그리드 라인 그리기
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = startY + (height / gridLines) * i
            drawLine(
                color = FinancialColors.OnSurfaceVariant.copy(alpha = 0.1f),
                start = Offset(startX, y),
                end = Offset(startX + width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 데이터 포인트 계산
        val points = values.mapIndexed { index, value ->
            val x = startX + index * stepX
            val y = startY + height - ((value - min) / range) * height
            Offset(x, y)
        }

        // 그라데이션 배경 영역
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
                colors = listOf(
                    FinancialColors.GraphPrimary.copy(alpha = 0.3f),
                    FinancialColors.GraphPrimary.copy(alpha = 0.05f)
                )
            )
        )

        // 메인 라인 그리기
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
        }

        drawPath(
            path = linePath,
            color = FinancialColors.GraphPrimary,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 데이터 포인트 원 그리기
        points.forEach { point ->
            drawCircle(
                color = FinancialColors.Surface,
                radius = 6.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = FinancialColors.GraphPrimary,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun ModernBarChart(data: Map<String, Float>) {
    if (data.isEmpty()) return

    val values = data.values.toList()
    val max = values.maxOrNull() ?: 0f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width - 40.dp.toPx()
        val height = size.height - 40.dp.toPx()
        val startX = 20.dp.toPx()
        val startY = 20.dp.toPx()

        val barWidth = (width / values.size) * 0.6f
        val barSpacing = (width / values.size) * 0.4f

        values.forEachIndexed { index, value ->
            val barHeight = (value / max) * height
            val x = startX + index * (barWidth + barSpacing) + barSpacing / 2
            val y = startY + height - barHeight

            // 그라데이션 막대
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        FinancialColors.GraphPrimary,
                        FinancialColors.GraphPrimary.copy(alpha = 0.7f)
                    )
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
fun ModernPieChart(data: Map<String, Float>) {
    if (data.isEmpty()) return

    val values = data.values.toList()
    val total = values.sum()
    val colors = listOf(
        FinancialColors.GraphPrimary,
        FinancialColors.GraphSecondary,
        FinancialColors.GraphTertiary,
        FinancialColors.GraphAccent,
        FinancialColors.Success,
        FinancialColors.Warning
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(centerX, centerY) * 0.8f
        val innerRadius = radius * 0.5f

        var startAngle = -90f

        values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            val color = colors[index % colors.size]

            // 외부 원호
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = (radius - innerRadius))
            )

            startAngle += sweepAngle
        }

        // 중앙 원
        drawCircle(
            color = FinancialColors.Surface,
            radius = innerRadius,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 현대적인 보고서 상세 화면
 */// ModernReportDetailScreen에서 개별 차트 섹션들 제거
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernReportDetailScreen(
    report: AnalystReport,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FinancialColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 히어로 헤더 섹션
            item {
                ModernHeroHeader(report = report)
            }

            // Executive Summary - 강조 텍스트
            item {
                EnhancedSummaryCard(
                    title = "Executive Summary",
                    content = report.summary
                )
            }

            // 주요 지표 카드들 - 3개 차트만 표시
            if (report.graphData.isNotEmpty()) {
                item {
                    ModernEconomicChartsSection(report = report)
                }
            }

            // 개별 차트 섹션들 모두 제거
            // report.graphData.forEach { graphData -> ... } - 이 부분 삭제

            // 상세 분석 내용 - 강조 텍스트
            item {
                EnhancedAnalysisCard(
                    title = "상세 분석",
                    content = report.detailedContent
                )
            }
        }

        // 플로팅 백 버튼
        FloatingActionButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp),
            containerColor = FinancialColors.Surface.copy(alpha = 0.9f),
            contentColor = FinancialColors.OnSurface
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 새로운 경제 차트 섹션 - 3개 차트만 표시
@Composable
fun ModernEconomicChartsSection(report: AnalystReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = "📊",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "주요 경제 지표",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinancialColors.OnSurface
                )
            }

            // 특정 차트 3개만 추출
            val targetCharts = extractTargetCharts(report.graphData)

            if (targetCharts.isNotEmpty()) {
                // 상단 2개 차트 (미국금리, 한국금리)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    targetCharts.find { it.title.contains("미국") && it.title.contains("금리") }?.let { usChart ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(180.dp),
                            colors = CardDefaults.cardColors(containerColor = FinancialColors.SurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "미국 연방금리",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialColors.OnSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    EnhancedBarChart(
                                        data = usChart.data,
                                        color = FinancialColors.Error,
                                        showAxes = true
                                    )
                                }
                            }
                        }
                    }

                    targetCharts.find { it.title.contains("한국") && it.title.contains("금리") }?.let { krChart ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(180.dp),
                            colors = CardDefaults.cardColors(containerColor = FinancialColors.SurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "한국 기준금리",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialColors.OnSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    EnhancedBarChart(
                                        data = krChart.data,
                                        color = FinancialColors.Primary,
                                        showAxes = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 하단 환율 차트 (전체 너비)
                targetCharts.find { it.title.contains("환율") }?.let { exchangeChart ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = CardDefaults.cardColors(containerColor = FinancialColors.SurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "원달러 환율 추이",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinancialColors.OnSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                EnhancedLineChart(
                                    data = exchangeChart.data,
                                    color = FinancialColors.Warning,
                                    showAxes = true
                                )
                            }
                        }
                    }
                }
            } else {
                // 데이터가 없을 경우 기본 메시지
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "경제 지표 데이터를 불러오는 중...",
                        color = FinancialColors.OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// 필요한 차트 3개만 추출하는 함수
fun extractTargetCharts(graphData: List<GraphData>): List<GraphData> {
    val targetCharts = mutableListOf<GraphData>()

    // 미국 금리 차트
    graphData.find { it.title.contains("미국") && it.title.contains("금리") }?.let {
        targetCharts.add(it)
    }

    // 한국 금리 차트
    graphData.find { it.title.contains("한국") && it.title.contains("금리") }?.let {
        targetCharts.add(it)
    }

    // 환율 차트
    graphData.find { it.title.contains("환율") }?.let {
        targetCharts.add(it)
    }

    return targetCharts
}

// 축이 포함된 개선된 막대 차트
@Composable
fun EnhancedBarChart(
    data: Map<String, Float>,
    color: Color,
    showAxes: Boolean = true
) {
    if (data.isEmpty()) return

    val values = data.values.toList()
    val keys = data.keys.toList()
    val max = values.maxOrNull() ?: 0f
    val min = values.minOrNull() ?: 0f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val startX = padding
        val startY = padding

        // Y축 그리기
        if (showAxes) {
            drawLine(
                color = FinancialColors.OnSurfaceVariant,
                start = Offset(startX, startY),
                end = Offset(startX, startY + chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            // X축 그리기
            drawLine(
                color = FinancialColors.OnSurfaceVariant,
                start = Offset(startX, startY + chartHeight),
                end = Offset(startX + chartWidth, startY + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        val barWidth = chartWidth / values.size * 0.6f
        val barSpacing = chartWidth / values.size * 0.4f

        values.forEachIndexed { index, value ->
            val barHeight = if (max > min) ((value - min) / (max - min)) * chartHeight else 0f
            val x = startX + index * (barWidth + barSpacing) + barSpacing / 2
            val y = startY + chartHeight - barHeight

            // 막대 그리기
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }

        // 축 레이블 그리기 (showAxes가 true일 때)
        if (showAxes && values.isNotEmpty()) {
            // Y축 값들 (최대, 최소값)
            drawContext.canvas.nativeCanvas.apply {
                // 색상을 Android Color로 변환
                val textColor = android.graphics.Color.argb(
                    255,
                    (FinancialColors.OnSurfaceVariant.red * 255).toInt(),
                    (FinancialColors.OnSurfaceVariant.green * 255).toInt(),
                    (FinancialColors.OnSurfaceVariant.blue * 255).toInt()
                )

                // Paint 객체를 새로 생성
                val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                textPaint.color = textColor

                // 최대값
                drawText(
                    String.format("%.1f", max),
                    startX - 8.dp.toPx(),
                    startY + textPaint.textSize / 2,
                    textPaint
                )

                // 최소값
                drawText(
                    String.format("%.1f", min),
                    startX - 8.dp.toPx(),
                    startY + chartHeight + textPaint.textSize / 2,
                    textPaint
                )
            }
        }
    }
}

// 축이 포함된 개선된 라인 차트
@Composable
fun EnhancedLineChart(
    data: Map<String, Float>,
    color: Color,
    showAxes: Boolean = true
) {
    if (data.isEmpty() || data.size < 2) return

    val values = data.values.toList()
    val keys = data.keys.toList()
    val max = values.maxOrNull() ?: 0f
    val min = values.minOrNull() ?: 0f
    val range = (max - min).takeIf { it != 0f } ?: 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val startX = padding
        val startY = padding

        // 축 그리기
        if (showAxes) {
            // Y축
            drawLine(
                color = FinancialColors.OnSurfaceVariant,
                start = Offset(startX, startY),
                end = Offset(startX, startY + chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            // X축
            drawLine(
                color = FinancialColors.OnSurfaceVariant,
                start = Offset(startX, startY + chartHeight),
                end = Offset(startX + chartWidth, startY + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        val stepX = chartWidth / (values.size - 1)

        // 데이터 포인트 계산
        val points = values.mapIndexed { index, value ->
            val x = startX + index * stepX
            val y = startY + chartHeight - ((value - min) / range) * chartHeight
            Offset(x, y)
        }

        // 그라데이션 배경 영역
        val gradientPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, startY + chartHeight)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, startY + chartHeight)
                close()
            }
        }

        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    color.copy(alpha = 0.05f)
                )
            )
        )

        // 메인 라인 그리기
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
        }

        drawPath(
            path = linePath,
            color = color,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 데이터 포인트 원 그리기
        points.forEach { point ->
            drawCircle(
                color = FinancialColors.Surface,
                radius = 4.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = color,
                radius = 2.dp.toPx(),
                center = point
            )
        }

        // 축 레이블 그리기
        if (showAxes) {
            drawContext.canvas.nativeCanvas.apply {
                // 색상을 Android Color로 변환
                val textColor = android.graphics.Color.argb(
                    255,
                    (FinancialColors.OnSurfaceVariant.red * 255).toInt(),
                    (FinancialColors.OnSurfaceVariant.green * 255).toInt(),
                    (FinancialColors.OnSurfaceVariant.blue * 255).toInt()
                )

                // Y축 값들용 Paint
                val yAxisPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                yAxisPaint.color = textColor

                drawText(
                    "${max.toInt()}",
                    startX - 8.dp.toPx(),
                    startY + yAxisPaint.textSize / 2,
                    yAxisPaint
                )

                drawText(
                    "${min.toInt()}",
                    startX - 8.dp.toPx(),
                    startY + chartHeight + yAxisPaint.textSize / 2,
                    yAxisPaint
                )

                // X축 레이블용 Paint
                val xAxisPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                xAxisPaint.color = textColor

                if (keys.isNotEmpty()) {
                    drawText(
                        keys.first(),
                        startX,
                        startY + chartHeight + 24.dp.toPx(),
                        xAxisPaint
                    )

                    drawText(
                        keys.last(),
                        startX + chartWidth,
                        startY + chartHeight + 24.dp.toPx(),
                        xAxisPaint
                    )
                }
            }
        }
    }
}

// 실제 데이터 기반 경제 지표 섹션
@Composable
fun ModernMetricsSectionFromData(report: AnalystReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = "📊",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "주요 경제 지표",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinancialColors.OnSurface
                )
            }

            // 실제 그래프 데이터에서 경제 지표 추출
            val economicMetrics = extractEconomicMetricsFromGraphData(report.graphData)

            if (economicMetrics.isNotEmpty()) {
                // 높이 계산 수정
                val gridHeight = ((economicMetrics.size + 1) / 2) * 160
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(gridHeight.dp)
                ) {
                    items(economicMetrics) { metric ->
                        EconomicMetricCard(metric = metric)
                    }
                }
            } else {
                // 데이터가 없을 경우 기본 메시지
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "경제 지표 데이터를 불러오는 중...",
                        color = FinancialColors.OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// 그래프 데이터에서 경제 지표 추출
fun extractEconomicMetricsFromGraphData(graphData: List<GraphData>): List<EconomicMetric> {
    val metrics = mutableListOf<EconomicMetric>()

    graphData.forEach { graph ->
        when {
            graph.title.contains("환율") -> {
                val valuesList = graph.data.values.toList()
                val latestValue = valuesList.lastOrNull() ?: 0f
                val previousValue = if (valuesList.size > 1) valuesList[valuesList.size - 2] else latestValue
                val change = latestValue - previousValue

                metrics.add(
                    EconomicMetric(
                        label = "원달러 환율",
                        value = "${latestValue.toInt()}원",
                        change = if (change >= 0) "+${change.toInt()}원" else "${change.toInt()}원",
                        isPositive = change <= 0, // 환율은 하락이 긍정적
                        color = if (change <= 0) FinancialColors.Success else FinancialColors.Error,
                        chartData = valuesList
                    )
                )
            }

            graph.title.contains("한국") && graph.title.contains("금리") -> {
                val valuesList = graph.data.values.toList()
                val latestValue = valuesList.lastOrNull() ?: 0f
                val previousValue = if (valuesList.size > 1) valuesList[valuesList.size - 2] else latestValue
                val change = latestValue - previousValue

                metrics.add(
                    EconomicMetric(
                        label = "한국 기준금리",
                        value = "${String.format("%.2f", latestValue)}%",
                        change = if (change >= 0) "+${String.format("%.2f", change)}%" else "${String.format("%.2f", change)}%",
                        isPositive = change <= 0, // 금리 하락이 주식시장에는 긍정적
                        color = FinancialColors.Primary,
                        chartData = valuesList
                    )
                )
            }

            graph.title.contains("미국") && graph.title.contains("금리") -> {
                val valuesList = graph.data.values.toList()
                val latestValue = valuesList.lastOrNull() ?: 0f
                val previousValue = if (valuesList.size > 1) valuesList[valuesList.size - 2] else latestValue
                val change = latestValue - previousValue

                metrics.add(
                    EconomicMetric(
                        label = "미국 연방금리",
                        value = "${String.format("%.2f", latestValue)}%",
                        change = if (change >= 0) "+${String.format("%.2f", change)}%" else "${String.format("%.2f", change)}%",
                        isPositive = change <= 0,
                        color = FinancialColors.Error,
                        chartData = valuesList
                    )
                )
            }

            graph.title.contains("주가") || graph.title.contains("주식") -> {
                val valuesList = graph.data.values.toList()
                val latestValue = valuesList.lastOrNull() ?: 0f
                val previousValue = if (valuesList.size > 1) valuesList[valuesList.size - 2] else latestValue
                val change = latestValue - previousValue
                val changePercent = if (previousValue != 0f) (change / previousValue) * 100 else 0f

                metrics.add(
                    EconomicMetric(
                        label = "주가 지수",
                        value = String.format("%.1f", latestValue),
                        change = if (changePercent >= 0) "+${String.format("%.1f", changePercent)}%" else "${String.format("%.1f", changePercent)}%",
                        isPositive = change >= 0,
                        color = if (change >= 0) FinancialColors.Success else FinancialColors.Error,
                        chartData = valuesList
                    )
                )
            }
        }
    }

    return metrics
}

@Composable
fun ModernHeroHeader(report: AnalystReport) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        FinancialColors.Primary,
                        FinancialColors.Primary.copy(alpha = 0.8f),
                        FinancialColors.Secondary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // 메타 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = report.category,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                Text(
                    text = dateFormat.format(report.date),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                ModernSentimentBadge(
                    sentiment = report.sentiment,
                    isDark = true
                )
            }

            // 제목
            Text(
                text = report.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModernSentimentBadge(
    sentiment: ReportSentiment,
    isDark: Boolean = false
) {
    val backgroundColor = if (isDark) {
        Color.White.copy(alpha = 0.2f)
    } else {
        sentiment.getColor().copy(alpha = 0.15f)
    }

    val contentColor = if (isDark) {
        Color.White
    } else {
        sentiment.getColor()
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = sentiment.getDisplayName(),
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// 강조된 요약 카드
@Composable
fun EnhancedSummaryCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            FinancialColors.Primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinancialColors.OnSurface
                )
            }

            // 번호가 있는 구조화된 요약 처리
            val summaryPoints = parseSummaryPoints(content)

            summaryPoints.forEachIndexed { index, point ->
                SummaryPointCard(
                    number = index + 1,
                    title = point.title,
                    details = point.details,
                    modifier = Modifier.padding(bottom = if (index < summaryPoints.size - 1) 16.dp else 0.dp)
                )
            }
        }
    }
}

data class SummaryPoint(
    val title: String,
    val details: List<String>
)

fun parseSummaryPoints(content: String): List<SummaryPoint> {
    val points = mutableListOf<SummaryPoint>()
    val lines = content.split("\n").filter { it.trim().isNotEmpty() }

    var currentTitle = ""
    var currentDetails = mutableListOf<String>()

    lines.forEach { line ->
        val trimmed = line.trim()
        when {
            // 숫자로 시작하는 제목 (1), 2), 3) 등)
            trimmed.matches(Regex("\\d+\\).*")) -> {
                // 이전 포인트 저장
                if (currentTitle.isNotEmpty()) {
                    points.add(SummaryPoint(currentTitle, currentDetails.toList()))
                }
                // 새 포인트 시작
                currentTitle = trimmed.substringAfter(") ").trim()
                currentDetails = mutableListOf()
            }
            // - 로 시작하는 세부사항
            trimmed.startsWith("- ") -> {
                currentDetails.add(trimmed.substring(2).trim())
            }
        }
    }

    // 마지막 포인트 저장
    if (currentTitle.isNotEmpty()) {
        points.add(SummaryPoint(currentTitle, currentDetails.toList()))
    }

    return points
}

@Composable
fun SummaryPointCard(
    number: Int,
    title: String,
    details: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FinancialColors.Primary.copy(alpha = 0.03f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // 번호 뱃지
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            FinancialColors.Primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 제목
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinancialColors.OnSurface,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // 세부사항들
            details.forEach { detail ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                FinancialColors.Primary.copy(alpha = 0.6f),
                                CircleShape
                            )
                            .padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = detail,
                        fontSize = 14.sp,
                        color = FinancialColors.OnSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// 강조된 분석 카드
@Composable
fun EnhancedAnalysisCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            FinancialColors.Secondary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📈",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinancialColors.OnSurface
                )
            }

            // • 로 시작하는 불릿 포인트들을 카테고리별로 그룹화
            val bulletPoints = parseContentBulletPoints(content)

            bulletPoints.forEachIndexed { index, bulletPoint ->
                BulletPointCard(
                    category = bulletPoint.category,
                    details = bulletPoint.details,
                    icon = getIconForCategory(bulletPoint.category),
                    color = getColorForCategory(bulletPoint.category),
                    modifier = Modifier.padding(bottom = if (index < bulletPoints.size - 1) 16.dp else 0.dp)
                )
            }
        }
    }
}

data class BulletPoint(
    val category: String,
    val details: List<String>
)

fun parseContentBulletPoints(content: String): List<BulletPoint> {
    val points = mutableListOf<BulletPoint>()
    val lines = content.split("\n").filter { it.trim().isNotEmpty() }

    var currentCategory = ""
    var currentDetails = mutableListOf<String>()

    lines.forEach { line ->
        val trimmed = line.trim()
        when {
            // • 로 시작하는 메인 카테고리
            trimmed.startsWith("• ") && !trimmed.startsWith("• -") -> {
                // 이전 카테고리 저장
                if (currentCategory.isNotEmpty()) {
                    points.add(BulletPoint(currentCategory, currentDetails.toList()))
                }
                // 새 카테고리 시작
                currentCategory = trimmed.substring(2).trim()
                currentDetails = mutableListOf()
            }
            // - 로 시작하는 세부사항
            trimmed.startsWith("- ") -> {
                currentDetails.add(trimmed.substring(2).trim())
            }
        }
    }

    // 마지막 카테고리 저장
    if (currentCategory.isNotEmpty()) {
        points.add(BulletPoint(currentCategory, currentDetails.toList()))
    }

    return points
}

@Composable
fun BulletPointCard(
    category: String,
    details: List<String>,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FinancialColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 카테고리 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // 세부사항들
            details.forEach { detail ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color.copy(alpha = 0.6f),
                                CircleShape
                            )
                            .padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = detail,
                        fontSize = 14.sp,
                        color = FinancialColors.OnSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun getIconForCategory(category: String): String {
    return ""  // 이모지 제거
}

fun getColorForCategory(category: String): Color {
    return when {
        category.contains("관세") || category.contains("무역") -> FinancialColors.Error
        category.contains("비트코인") || category.contains("암호화폐") -> FinancialColors.Warning
        category.contains("유가") || category.contains("OPEC") -> FinancialColors.GraphAccent
        category.contains("고용") || category.contains("일자리") -> FinancialColors.Primary
        category.contains("금리") || category.contains("일본은행") -> FinancialColors.GraphSecondary
        category.contains("지정학") || category.contains("군사") -> FinancialColors.Error
        category.contains("현대") || category.contains("조선") -> FinancialColors.Success
        category.contains("더본코리아") || category.contains("외식") -> FinancialColors.GraphTertiary
        category.contains("인수합병") || category.contains("투자") -> FinancialColors.Secondary
        else -> FinancialColors.Neutral
    }
}

@Composable
fun ModernMetricsSection(report: AnalystReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = "📊",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "주요 경제 지표",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinancialColors.OnSurface
                )
            }

            // 경제 지표 그리드 - 실제 데이터로 변경
            val economicMetrics = listOf(
                EconomicMetric(
                    "한국 금리",
                    "3.50%",
                    "+0.25%",
                    true,
                    FinancialColors.Primary,
                    generateKoreanRateData()
                ),
                EconomicMetric(
                    "미국 금리",
                    "5.25%",
                    "+0.50%",
                    true,
                    FinancialColors.Error,
                    generateUSRateData()
                ),
                EconomicMetric(
                    "원달러 환율",
                    "1,340원",
                    "+15원",
                    false,
                    FinancialColors.Warning,
                    generateExchangeRateData()
                ),
                EconomicMetric(
                    "공포지수(VIX)",
                    "18.5",
                    "-2.1",
                    true,
                    FinancialColors.Success,
                    generateVixData()
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(320.dp)
            ) {
                items(economicMetrics) { metric ->
                    EconomicMetricCard(metric = metric)
                }
            }
        }
    }
}

data class EconomicMetric(
    val label: String,
    val value: String,
    val change: String,
    val isPositive: Boolean,
    val color: Color,
    val chartData: List<Float>
)

@Composable
fun EconomicMetricCard(metric: EconomicMetric) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = FinancialColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = metric.label,
                        fontSize = 12.sp,
                        color = FinancialColors.OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = metric.value,
                        fontSize = 16.sp,
                        color = metric.color,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 변화량 표시
                Box(
                    modifier = Modifier
                        .background(
                            if (metric.isPositive) FinancialColors.Success.copy(alpha = 0.1f)
                            else FinancialColors.Error.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = metric.change,
                        fontSize = 10.sp,
                        color = if (metric.isPositive) FinancialColors.Success else FinancialColors.Error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 미니 차트
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MiniSparklineChart(
                    data = metric.chartData,
                    color = metric.color
                )
            }
        }
    }
}

@Composable
fun MiniSparklineChart(
    data: List<Float>,
    color: Color
) {
    if (data.isEmpty() || data.size < 2) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val max = data.maxOrNull() ?: 0f
        val min = data.minOrNull() ?: 0f
        val range = (max - min).takeIf { it != 0f } ?: 1f

        val stepX = width / (data.size - 1)

        // 데이터 포인트 계산
        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - min) / range) * height
            Offset(x, y)
        }

        // 그라데이션 영역
        val gradientPath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, height)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, height)
                close()
            }
        }

        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    color.copy(alpha = 0.1f),
                    Color.Transparent
                )
            )
        )

        // 라인
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
            }
        }

        drawPath(
            path = linePath,
            color = color,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// 데이터 생성 함수들
fun generateKoreanRateData(): List<Float> {
    return listOf(2.8f, 3.0f, 3.1f, 3.25f, 3.4f, 3.5f, 3.5f, 3.5f)
}

fun generateUSRateData(): List<Float> {
    return listOf(4.0f, 4.5f, 4.8f, 5.0f, 5.1f, 5.25f, 5.3f, 5.25f)
}

fun generateExchangeRateData(): List<Float> {
    return listOf(1280f, 1295f, 1315f, 1325f, 1340f, 1350f, 1345f, 1340f)
}

fun generateVixData(): List<Float> {
    return listOf(25.2f, 22.1f, 20.5f, 18.9f, 19.5f, 18.8f, 18.2f, 18.5f)
}

@Composable
fun ModernChartSection(
    graphData: GraphData
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = graphData.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FinancialColors.OnSurface,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        FinancialColors.SurfaceVariant,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                when (graphData.type) {
                    "LINE_CHART" -> ModernLineChart(data = graphData.data)
                    "BAR_CHART" -> ModernBarChart(data = graphData.data)
                    "PIE_CHART" -> ModernPieChart(data = graphData.data)
                }
            }
        }
    }
}

data class InvestmentOpportunity(
    val icon: String,
    val title: String,
    val description: String,
    val color: Color
)

data class RiskFactor(
    val icon: String,
    val title: String,
    val description: String,
    val level: String,
    val color: Color
)

// 확장 함수들
fun ReportSentiment.getColor(): Color = when (this) {
    ReportSentiment.POSITIVE -> FinancialColors.Success
    ReportSentiment.NEGATIVE -> FinancialColors.Error
    ReportSentiment.NEUTRAL -> FinancialColors.Neutral
    ReportSentiment.CAUTION -> FinancialColors.Warning
}

fun ReportSentiment.getDisplayName(): String = when (this) {
    ReportSentiment.POSITIVE -> "긍정적"
    ReportSentiment.NEGATIVE -> "부정적"
    ReportSentiment.NEUTRAL -> "중립적"
    ReportSentiment.CAUTION -> "주의"
}