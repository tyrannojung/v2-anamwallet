package com.anam145.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.navigation.AnamBottomNavigation
import com.anam145.wallet.navigation.AnamNavHost
import com.anam145.wallet.navigation.AnamNavRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnamWalletTheme {
                AnamWalletApp()
            }
        }
    }
}

/**
 * ANAM Wallet 메인 앱 컴포저블
 * 
 * Navigation Controller를 생성하고 Bottom Navigation과
 * NavHost를 포함한 Scaffold를 구성.
 */
@Composable
fun AnamWalletApp() {
    // Navigation Controller 생성
    val navController = rememberNavController()
    
    // 현재 경로 추적
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentNavRoute = AnamNavRoute.fromRoute(currentRoute)
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // 상단 헤더
            Header(
                // stringResource : Compose에서 문자열 리소스를 가져오는 함수
                // ?: 연산자 -> null일때 기본값 제공
                title = stringResource(currentNavRoute?.titleRes ?: R.string.header_title),
                showBlockchainStatus = currentRoute == AnamNavRoute.Main.route,
                blockchainConnected = false // TODO: 실제 블록체인 상태로 교체
            )
        },
        bottomBar = {
            // Bottom Navigation Bar
            AnamBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        // Navigation Host - 모든 화면들을 관리
        AnamNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}