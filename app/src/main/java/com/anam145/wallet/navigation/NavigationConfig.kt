package com.anam145.wallet.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation 관련 모든 설정을 중앙 관리
 * 
 * Single Source of Truth 원칙에 따라 네비게이션 라우트, 아이콘, 
 * Bottom Navigation 설정 등을 한 곳에서 관리합니다.
 */
object NavigationConfig {
    
    /**
     * Navigation 아이콘 정보
     */
    data class NavIconInfo(
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
        val labelKey: String
    )
    
    /**
     * 각 라우트별 아이콘 매핑
     *   mapOf는 Pair들을 받아서 Map을 만듦
     *   mapOf(
     *       key1 to value1,  // Pair(key1, value1)
     *       key2 to value2   // Pair(key2, value2)
     *   )
     *
     *   결과: Map<K, V>
     *   navigationIconMap[AnamNavRoute.Main] = NavIconInfo(...)
     */
    private val iconMap = mapOf(
        AnamNavRoute.Main to NavIconInfo(
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            labelKey = "main"
        ),
        AnamNavRoute.Hub to NavIconInfo(
            selectedIcon = Icons.Filled.Hub,
            unselectedIcon = Icons.Outlined.Hub,
            labelKey = "hub"
        ),
        AnamNavRoute.Browser to NavIconInfo(
            selectedIcon = Icons.Filled.Language,
            unselectedIcon = Icons.Outlined.Language,
            labelKey = "browser"
        ),
        // DID 기능 임시 비활성화
        // AnamNavRoute.Identity to NavIconInfo(
        //     selectedIcon = Icons.Filled.QrCode,
        //     unselectedIcon = Icons.Outlined.QrCode,
        //     labelKey = "identity"
        // ),
        AnamNavRoute.Settings to NavIconInfo(
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            labelKey = "settings"
        )
    )
    
    /**
     * Bottom Navigation에 표시될 라우트 목록
     * 순서대로 하단 탭에 표시됨.
     */
    private val bottomNavRoutes = listOf(
        AnamNavRoute.Main,
        AnamNavRoute.Hub,
        AnamNavRoute.Browser,
        // AnamNavRoute.Identity,  // DID 기능 임시 비활성화
        AnamNavRoute.Settings
    )
    
    /**
     * Bottom Navigation 아이템 목록 생성
     * 라우트와 아이콘 정보를 결합하여 BottomNavItem 리스트 반환
     */
    fun getBottomNavItems(): List<BottomNavItem> {
        return bottomNavRoutes.mapNotNull { route ->
            iconMap[route]?.let { iconInfo ->
                BottomNavItem(
                    route = route,
                    selectedIcon = iconInfo.selectedIcon,
                    unselectedIcon = iconInfo.unselectedIcon,
                    labelKey = iconInfo.labelKey
                )
            }
        }
    }
    
}