package com.bigPicture.businessreportgenerator.presentation.features.news

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NewsScreen(modifier: Modifier = Modifier) {
    val tabs = listOf("전체", "경제", "주식", "부동산", "암호화폐", "국제", "정책")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedNewsItem by remember { mutableStateOf<NewsItem?>(null) }

    // 뉴스 데이터 필터링
    val newsList = when (selectedTabIndex) {
        0 -> DummyNewsData.news // 전체
        1 -> DummyNewsData.news.filter { it.category == NewsCategory.ECONOMY } // 경제
        2 -> DummyNewsData.news.filter { it.category == NewsCategory.STOCK_MARKET } // 주식
        3 -> DummyNewsData.news.filter { it.category == NewsCategory.REAL_ESTATE } // 부동산
        4 -> DummyNewsData.news.filter { it.category == NewsCategory.CRYPTO } // 암호화폐
        5 -> DummyNewsData.news.filter { it.category == NewsCategory.INTERNATIONAL } // 국제
        6 -> DummyNewsData.news.filter { it.category == NewsCategory.POLICY } // 정책
        else -> DummyNewsData.news
    }

    val newsViewModel : NewsViewModel = viewModel()
    val exchangeState by newsViewModel.exchange.collectAsState()
    val ko = exchangeState.ko.toString()

    val listState = rememberLazyListState()
    val hideExchangeRate by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 100 }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF8F9FA) // 애플 스타일 배경색 (밝은 회색)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 현대적인 상단 헤더
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column {
                    //환율 부분
                    AnimatedVisibility(visible = !hideExchangeRate) {
                        ExchangeRateCard(
                            koRate = ko,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    // 탭 영역
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 16.dp,
                        containerColor = Color.White,
                        contentColor = Color(0xFF007AFF),
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                Box(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .height(3.dp)
                                        .padding(horizontal = 16.dp)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                        .background(Color(0xFF007AFF))
                                )
                            }
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = if (selectedTabIndex == index) Color(0xFF007AFF) else Color.Gray
                                    )
                                },
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            // 뉴스 목록 (기존 코드 그대로 유지)
            if (newsList.isEmpty()) {
                // 뉴스가 없을 때
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "해당 카테고리의 뉴스가 없습니다",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(newsList) { newsItem ->
                        NewsItemCard(
                            newsItem = newsItem,
                            onClick = { selectedNewsItem = newsItem }
                        )
                    }

                    // 하단 여백
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // 뉴스 상세 대화상자
        selectedNewsItem?.let { newsItem ->
            NewsDetailDialog(
                newsItem = newsItem,
                onDismiss = { selectedNewsItem = null }
            )
        }
    }
}

@Composable
fun NewsItemCard(
    newsItem: NewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), // 더 둥근 모서리
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // 더 섬세한 그림자
    ) {
        Column(
            modifier = Modifier.padding(20.dp) // 여백 증가
        ) {
            // 카테고리 및 출처
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 카테고리 태그
                Box(
                    modifier = Modifier
                        .background(
                            color = newsItem.category.getColor().copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp) // 더 둥근 모서리
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp) // 여백 증가
                ) {
                    Text(
                        text = newsItem.category.getDisplayName(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = newsItem.category.getColor()
                    )
                }

                // 출처 및 시간
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = newsItem.source,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 구분점
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(Color.LightGray, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 발행 시간
                    val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                    Text(
                        text = formatter.format(newsItem.publishedAt),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // 간격 증가

            // 제목
            Text(
                text = newsItem.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp // 줄 간격 추가
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 요약
            Text(
                text = newsItem.summary,
                fontSize = 15.sp,
                color = Color.DarkGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp // 줄 간격 추가
            )
        }
    }
}

@Composable
fun ExchangeRateItem(
    country: String,
    rate: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = country,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${rate} 원/$",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF007AFF)
        )
    }
}


@Composable
fun ExchangeRateCard(
    koRate: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "오늘의 환율",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ExchangeRateItem(country = "🇰🇷 오늘의 원-달러 환율", rate = koRate)
            }
        }
    }
}
