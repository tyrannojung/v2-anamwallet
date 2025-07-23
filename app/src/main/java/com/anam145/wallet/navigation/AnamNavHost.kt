package com.anam145.wallet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anam145.wallet.feature.main.ui.MainScreen
import com.anam145.wallet.feature.main.ui.MainViewModel
import com.anam145.wallet.feature.hub.ui.HubScreen
import com.anam145.wallet.feature.browser.BrowserScreen
import com.anam145.wallet.feature.identity.IdentityScreen
import com.anam145.wallet.feature.settings.ui.SettingsScreen
import com.anam145.wallet.feature.miniapp.webapp.WebAppActivity
import com.anam145.wallet.feature.miniapp.blockchain.BlockchainActivity
import com.anam145.wallet.feature.auth.ui.login.LoginScreen
import com.anam145.wallet.feature.auth.ui.setup.SetupPasswordScreen

/**
 * ANAM Wallet의 메인 Navigation Host
 * 
 * 모든 화면의 네비게이션을 관리하며, 각 경로에 대한
 * Composable 화면을 연결합니다.
 * 
 * @param navController 네비게이션 컨트롤러
 * @param mainViewModel 공유되는 MainViewModel
 * @param startDestination 시작 화면 (기본값: Main)
 */
@Composable
fun AnamNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
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
            val context = LocalContext.current
            
            MainScreen(
                viewModel = mainViewModel,  // 공유 ViewModel 전달
                onNavigateToMiniApp = { appId ->
                    // MiniApp Activity 실행 (메인 프로세스)
                    val intent = WebAppActivity.createIntent(context, appId)
                    context.startActivity(intent)
                },
                onLaunchBlockchain = { blockchainId ->
                    // 블록체인 Activity 실행 (블록체인 프로세스)
                    val intent = BlockchainActivity.createIntent(context, blockchainId)
                    context.startActivity(intent)
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
        
        // ========== 인증 화면들 ==========
        
        // 로그인 화면
        composable(route = AnamNavRoute.Login.route) {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate(AnamNavRoute.Main.route) {
                        popUpTo(AnamNavRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 비밀번호 설정 화면
        composable(route = AnamNavRoute.SetupPassword.route) {
            SetupPasswordScreen(
                onNavigateToMain = {
                    navController.navigate(AnamNavRoute.Main.route) {
                        popUpTo(AnamNavRoute.SetupPassword.route) { inclusive = true }
                    }
                }
            )
        }
    }
}