package com.anam145.wallet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * ANAM Wallet의 메인 Navigation Host
 * 
 * 모든 화면의 네비게이션을 관리하며, 각 경로에 대한
 * Composable 화면을 연결합니다.
 * 
 * @param navController 네비게이션 컨트롤러
 * @param startDestination 시작 화면 (기본값: Main)
 */
@Composable
fun AnamNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AnamNavRoute.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ========== Bottom Navigation 화면들 ==========
        
        // 메인 화면
        composable(route = AnamNavRoute.Main.route) {
            // TODO: Feature 모듈이 준비되면 실제 화면으로 교체
            PlaceholderScreen(title = "메인 화면")
        }
        
        // 허브 화면
        composable(route = AnamNavRoute.Hub.route) {
            PlaceholderScreen(title = "허브 화면")
        }
        
        // 브라우저 화면
        composable(route = AnamNavRoute.Browser.route) {
            PlaceholderScreen(title = "브라우저 화면")
        }
        
        // 신원 화면
        composable(route = AnamNavRoute.Identity.route) {
            PlaceholderScreen(title = "신원 화면")
        }
        
        // 설정 화면
        composable(route = AnamNavRoute.Settings.route) {
            PlaceholderScreen(title = "설정 화면")
        }
        
        // ========== 상세 화면들 ==========
        
        // 미니앱 상세 화면
        composable(
            route = AnamNavRoute.MiniAppDetail.route,
            arguments = listOf(
                navArgument("appId") { 
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: ""
            PlaceholderScreen(title = "미니앱: $appId")
        }
        
        // 모듈 상세 화면
        composable(
            route = AnamNavRoute.ModuleDetail.route,
            arguments = listOf(
                navArgument("moduleId") { 
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
            PlaceholderScreen(title = "모듈: $moduleId")
        }
        
        // 학생증 상세 화면
        composable(route = AnamNavRoute.StudentCardDetail.route) {
            PlaceholderScreen(title = "학생증 상세")
        }
    }
}

/**
 * 임시 Placeholder 화면
 * Feature 모듈이 구현되기 전까지 사용할 임시 화면입니다.
 * 
 * @param title 화면에 표시할 제목
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}