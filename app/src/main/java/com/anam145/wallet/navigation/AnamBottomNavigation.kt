package com.anam145.wallet.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom Navigation Bar 아이템 정의
 * 
 * @property route 네비게이션 경로
 * @property selectedIcon 선택된 상태의 아이콘
 * @property unselectedIcon 선택되지 않은 상태의 아이콘
 * @property label 표시될 라벨 텍스트
 */
data class BottomNavItem(
    val route: AnamNavRoute,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

/**
 * Bottom Navigation 아이템 목록
 * 기존 anam-android와 동일한 아이콘 사용
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = AnamNavRoute.Main,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "홈"
    ),
    BottomNavItem(
        route = AnamNavRoute.Hub,
        selectedIcon = Icons.Filled.Face,
        unselectedIcon = Icons.Outlined.Face,
        label = "허브"
    ),
    BottomNavItem(
        route = AnamNavRoute.Browser,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        label = "브라우저"
    ),
    BottomNavItem(
        route = AnamNavRoute.Identity,
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        label = "신원"
    ),
    BottomNavItem(
        route = AnamNavRoute.Settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "설정"
    )
)

/**
 * ANAM Wallet Bottom Navigation Bar
 * 
 * Material 3 NavigationBar를 사용하여 하단 네비게이션을 구현합니다.
 * 현재 선택된 경로에 따라 아이콘이 filled/outlined로 변경됩니다.
 * 
 * @param navController 네비게이션 컨트롤러
 */
@Composable
fun AnamBottomNavigation(
    navController: NavController
) {
    // 현재 선택된 경로 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route.route
            
            NavigationBarItem(
                selected = selected,
                onClick = {
                    // 이미 선택된 탭을 다시 누르면 스택을 초기화
                    navController.navigate(item.route.route) {
                        // 시작 화면까지 모든 화면을 팝
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // 동일한 화면이 여러 번 스택에 쌓이지 않도록 함
                        launchSingleTop = true
                        // 이전 상태 복원
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                }
            )
        }
    }
}