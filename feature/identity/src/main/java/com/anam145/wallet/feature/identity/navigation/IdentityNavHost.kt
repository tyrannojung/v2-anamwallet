package com.anam145.wallet.feature.identity.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anam145.wallet.feature.identity.ui.IdentityScreen
import com.anam145.wallet.feature.identity.ui.components.StudentCardDetail
import com.anam145.wallet.feature.identity.ui.components.DriverLicenseDetail
import com.anam145.wallet.feature.identity.ui.components.IssueSelect

/**
 * Identity 기능의 Nested Navigation Host
 * 
 * Identity 탭 내에서의 화면 전환을 관리합니다.
 * 이를 통해 상세 화면으로 이동해도 하단 네비게이션 바의
 * Identity 탭이 활성화된 상태를 유지할 수 있습니다.
 */
@Composable
fun IdentityNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = IdentityRoute.Main.route,
        modifier = modifier
    ) {
        // 메인 Identity 화면
        composable(route = IdentityRoute.Main.route) {
            IdentityScreen(
                onNavigateToStudentCard = {
                    navController.navigate(IdentityRoute.StudentCard.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToDriverLicense = {
                    navController.navigate(IdentityRoute.DriverLicense.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToIssue = {
                    navController.navigate(IdentityRoute.IssueSelect.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // 발급 선택 화면
        composable(route = IdentityRoute.IssueSelect.route) {
            IssueSelect(
                onBackClick = {
                    navController.popBackStack()
                },
                onSelectStudentCard = {
                    // TODO: 학생증 발급 프로세스 시작
                    navController.popBackStack()
                },
                onSelectDriverLicense = {
                    // TODO: 운전면허증 발급 프로세스 시작
                    navController.popBackStack()
                }
            )
        }
        
        // 학생증 상세 화면
        composable(route = IdentityRoute.StudentCard.route) {
            StudentCardDetail(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 운전면허증 상세 화면
        composable(route = IdentityRoute.DriverLicense.route) {
            DriverLicenseDetail(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Identity 내부 네비게이션 경로
 */
sealed class IdentityRoute(val route: String) {
    data object Main : IdentityRoute("identity_main")
    data object IssueSelect : IdentityRoute("identity_issue_select")
    data object StudentCard : IdentityRoute("identity_student_card")
    data object DriverLicense : IdentityRoute("identity_driver_license")
}