package com.bigPicture.businessreportgenerator.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector

// 현대적인 네비게이션 항목
sealed class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val title: String,
    val description: String
) {
    object Portfolio : NavigationItem(
        route = "portfolio",
        icon = Icons.Rounded.AccountCircle,
        title = "포트폴리오",
        description = "내 자산 현황"
    )

    object Analyst : NavigationItem(
        route = "analyst",
        icon = Icons.Rounded.Search,
        title = "AI 애널리스트",
        description = "맞춤 분석"
    )


    object Board : NavigationItem(
        route = "community",
        icon = Icons.Rounded.Face,
        title = "커뮤니티",
        description = "투자 토론"
    )
}

// 업데이트된 네비게이션 아이템 리스트
val modernNavigationItems = listOf(
    NavigationItem.Portfolio,
    NavigationItem.Analyst,
    NavigationItem.Board
)

// 기존 호환성을 위한 items (deprecated)
@Deprecated("Use modernNavigationItems instead")
val items = listOf(
    NavigationItem.Portfolio,
    NavigationItem.Analyst,
    NavigationItem.Board
)