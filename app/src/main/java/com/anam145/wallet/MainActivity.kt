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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.ui.components.Header
import com.anam145.wallet.navigation.AnamBottomNavigation
import com.anam145.wallet.navigation.AnamNavHost
import com.anam145.wallet.navigation.AnamNavRoute
import com.anam145.wallet.ui.theme.ThemeViewModel
import com.anam145.wallet.ui.language.LanguageViewModel
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.language.getStringsForLanguage
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel

// Hilt가 의존성을 주입하는 시작점
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnamWalletApp()
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
    // 테마 ViewModel
    // hiltViewModel() → ViewModel 인스턴스 생성
    val themeViewModel: ThemeViewModel = hiltViewModel()
    // collectAsStateWithLifecycle : 화면이 보일 때만 수집!
    /**
     * 1. 사용자가 앱 사용 중 (수집 중 ✓)
     * 2. 홈 버튼 → 앱이 백그라운드로
     * 3. 하지만 여전히 데이터 수집 중...
     * 4. 배터리 낭비 + 불필요한 연산 -> collectAsStateWithLifecycle 사용한 이유!
     * by 문법 = "대신해줘!"
     *
     * ex)
     * 이 귀찮은 일을
     * val box = State(10)
     * println(box.value)
     * box.value = 20
     *
     * by가 대신해줌
     * var number by State(10)
     * println(number)  // 알아서 .value
     * number = 20      // 알아서 .value =
     * */
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    
    // 언어 ViewModel
    val languageViewModel: LanguageViewModel = hiltViewModel()
    val language by languageViewModel.language.collectAsStateWithLifecycle()
    val strings = getStringsForLanguage(language)

    /**
     * CompositionLocal로 언어와 문자열 제공
     * CompositionLocal은 Compose의 "암시적 데이터 전달" 메커니즘.
     * 모든 하위 컴포저블에서 매개변수로 전달하지 않고도 데이터에 접근할 수 있게 해줌
     */
    CompositionLocalProvider(
        LocalStrings provides strings
    ) {
        AnamWalletTheme(themeMode = themeMode) {
            // Navigation Controller 생성
            val navController = rememberNavController()
            
            // 현재 경로 추적
            // Navigation Compose에서 현재 화면의 상태를 관찰하는 함수
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route // "main", "hub" 등 문자열
            val currentNavRoute = AnamNavRoute.fromRoute(currentRoute) // 문자열 → 객체 변환
            
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    // 상단 헤더
                    Header(
                        // LocalStrings로 언어별 타이틀 제공
                        title = when (currentNavRoute) {
                            AnamNavRoute.Main -> strings.headerTitleMain
                            AnamNavRoute.Hub -> strings.headerTitleHub
                            AnamNavRoute.Browser -> strings.headerTitleBrowser
                            AnamNavRoute.Identity -> strings.headerTitleIdentity
                            AnamNavRoute.Settings -> strings.headerTitleSettings
                            else -> strings.headerTitle
                        },
                        showBlockchainStatus = currentRoute == AnamNavRoute.Main.route,
                        blockchainConnected = false
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
    }
}