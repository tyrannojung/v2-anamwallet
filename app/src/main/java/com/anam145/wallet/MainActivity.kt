package com.anam145.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.navigation.AnamBottomNavigation
import com.anam145.wallet.navigation.AnamNavHost

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
 * NavHost를 포함한 Scaffold를 구성합니다.
 */
@Composable
fun AnamWalletApp() {
    // Navigation Controller 생성
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
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