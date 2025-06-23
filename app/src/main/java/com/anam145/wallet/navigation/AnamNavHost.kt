package com.anam145.wallet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anam145.wallet.feature.main.ui.MainScreen
import com.anam145.wallet.feature.hub.HubScreen
import com.anam145.wallet.feature.browser.BrowserScreen
import com.anam145.wallet.feature.identity.IdentityScreen
import com.anam145.wallet.feature.settings.ui.SettingsScreen

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
            MainScreen(
                onNavigateToHub = { 
                    navController.navigate(AnamNavRoute.Hub.route) {
                        // Use the same navigation options as bottom navigation
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToMiniApp = { appId ->
                    // TODO: Navigate to mini app detail
                    android.util.Log.d("AnamNavHost", "Navigate to mini app: $appId")
                },
                onLaunchBlockchain = { blockchainId ->
                    // TODO: Launch blockchain activity
                    android.util.Log.d("AnamNavHost", "Launch blockchain: $blockchainId")
                }
            )
        }
        
        // 허브 화면
        composable(route = AnamNavRoute.Hub.route) {
            HubScreen()
        }
        
        // 브라우저 화면
        composable(route = AnamNavRoute.Browser.route) {
            BrowserScreen()
        }
        
        // 신원 화면
        composable(route = AnamNavRoute.Identity.route) {
            IdentityScreen()
        }
        
        // 설정 화면
        composable(route = AnamNavRoute.Settings.route) {
            SettingsScreen()
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
            // TODO: MiniAppDetailScreen(appId)
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
            // TODO: ModuleDetailScreen(moduleId)
        }
        
        // 학생증 상세 화면
        composable(route = AnamNavRoute.StudentCardDetail.route) {
            // TODO: StudentCardDetailScreen()
        }
    }
}