package com.anam145.wallet.feature.settings.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anam145.wallet.core.ui.navigation.animatedComposable
import com.anam145.wallet.feature.settings.ui.SettingsScreen
import com.anam145.wallet.feature.settings.ui.help.HelpScreen
import com.anam145.wallet.feature.settings.ui.faq.FAQScreen
import com.anam145.wallet.feature.settings.ui.appinfo.AppInfoScreen
import com.anam145.wallet.feature.settings.ui.license.LicenseScreen

/**
 * Settings 기능의 Nested Navigation Host
 * 
 * Settings 탭 내에서의 화면 전환을 관리합니다.
 * 이를 통해 상세 화면으로 이동해도 하단 네비게이션 바의
 * Settings 탭이 활성화된 상태를 유지할 수 있습니다.
 */
@Composable
fun SettingsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SettingsRoute.Main.route,
        modifier = modifier
    ) {
        // 메인 Settings 화면
        animatedComposable(route = SettingsRoute.Main.route) {
            SettingsScreen(
                onNavigateToHelp = {
                    navController.navigate(SettingsRoute.Help.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToFAQ = {
                    navController.navigate(SettingsRoute.FAQ.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAppInfo = {
                    navController.navigate(SettingsRoute.AppInfo.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToLicense = {
                    navController.navigate(SettingsRoute.License.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // 도움말 화면
        animatedComposable(route = SettingsRoute.Help.route) {
            HelpScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // FAQ 화면
        animatedComposable(route = SettingsRoute.FAQ.route) {
            FAQScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 앱 정보 화면
        animatedComposable(route = SettingsRoute.AppInfo.route) {
            AppInfoScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 라이선스 화면
        animatedComposable(route = SettingsRoute.License.route) {
            LicenseScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}