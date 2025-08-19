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
import com.anam145.wallet.feature.browser.ui.BrowserScreen
import com.anam145.wallet.feature.identity.navigation.IdentityNavHost
import com.anam145.wallet.feature.settings.navigation.SettingsNavHost
import com.anam145.wallet.feature.miniapp.webapp.WebAppActivity
import com.anam145.wallet.feature.miniapp.blockchain.BlockchainActivity
import com.anam145.wallet.feature.auth.ui.login.LoginScreen
import com.anam145.wallet.feature.auth.ui.setup.SetupPasswordScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.ui.language.LanguageViewModel
import com.anam145.wallet.core.ui.navigation.animatedComposable

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
        animatedComposable(route = AnamNavRoute.Main.route) {
            val context = LocalContext.current
            val uiState by mainViewModel.uiState.collectAsState()
            val currentSkin = uiState.currentSkin
            
            // 언어 정보도 함께 가져오기
            val languageViewModel: LanguageViewModel = hiltViewModel()
            val currentLanguage by languageViewModel.language.collectAsStateWithLifecycle()
            
            MainScreen(
                viewModel = mainViewModel,  // 공유 ViewModel 전달
                onNavigateToMiniApp = { appId ->
                    // MiniApp Activity 실행 (메인 프로세스)
                    val intent = WebAppActivity.createIntent(context, appId, currentSkin, currentLanguage)
                    context.startActivity(intent)
                },
                onLaunchBlockchain = { blockchainId ->
                    // 블록체인 Activity 실행 (블록체인 프로세스)
                    val intent = BlockchainActivity.createIntent(context, blockchainId, currentSkin, currentLanguage)
                    context.startActivity(intent)
                }
            )
        }
        
        // 허브 화면
        animatedComposable(route = AnamNavRoute.Hub.route) {
            HubScreen()
        }
        
        // 브라우저 화면
        animatedComposable(route = AnamNavRoute.Browser.route) {
            BrowserScreen()
        }
        
        // 신원 화면 (Nested Navigation)
        animatedComposable(route = AnamNavRoute.Identity.route) {
            IdentityNavHost()
        }
        
        // 설정 화면 (Nested Navigation)
        animatedComposable(route = AnamNavRoute.Settings.route) {
            SettingsNavHost()
        }
        
        // ========== 상세 화면들 ==========
        
        // 미니앱 상세 화면
        animatedComposable(
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
        animatedComposable(
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
        
        // Identity 상세 화면들은 IdentityNavHost에서 처리
        // 기존 route들은 호환성을 위해 남겨둠 수 있음
        
        // ========== 인증 화면들 ==========
        
        // 로그인 화면
        animatedComposable(route = AnamNavRoute.Login.route) {
            LoginScreen(
                onNavigateToMain = {
                    navController.navigate(AnamNavRoute.Main.route) {
                        popUpTo(AnamNavRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // 비밀번호 설정 화면
        animatedComposable(route = AnamNavRoute.SetupPassword.route) {
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