package com.anam145.wallet.feature.main.ui

import com.anam145.wallet.core.common.model.MiniApp

/**
 * Main 화면의 Contract 정의
 * 
 * MVI (Model-View-Intent) 패턴을 사용하여 화면의 상태, 사용자 의도, 부수 효과를 정의합니다.
 */
interface MainContract {
    
    /**
     * UI 상태 - 화면에 표시되는 지속적인 데이터
     */
    data class MainState(
        val isLoading: Boolean = false,
        val isSyncing: Boolean = false,  // 초기화 동기화 중
        val blockchainApps: List<MiniApp> = emptyList(),
        val regularApps: List<MiniApp> = emptyList(),
        val activeBlockchainId: String? = null,
        val error: String? = null
    ) {
        // 앱이 없는 상태인지 확인 (로딩/동기화 중이 아닐 때만)
        val isEmpty: Boolean
            get() = !isLoading && !isSyncing && blockchainApps.isEmpty() && regularApps.isEmpty()
    }
    
    /**
     * 사용자 의도 - 사용자의 액션
     */
    sealed interface MainIntent {
        /** 미니앱 목록 로드 */
        data object LoadMiniApps : MainIntent
        
        /** 블록체인 앱 클릭 */
        data class ClickBlockchainApp(val miniApp: MiniApp) : MainIntent
        
        /** 일반 앱 클릭 */
        data class ClickRegularApp(val miniApp: MiniApp) : MainIntent
        
        /** 더 많은 서비스 추가 클릭 */
        data object ClickAddMore : MainIntent
    }
    
    /**
     * 부수 효과 - 시스템 레벨의 일회성 동작
     */
    sealed interface MainEffect {
        /** 블록체인 액티비티 실행 */
        data class LaunchBlockchainActivity(val blockchainId: String) : MainEffect
        
        /** 미니앱 화면으로 이동 */
        data class NavigateToMiniApp(val appId: String) : MainEffect
        
        /** Hub 화면으로 이동 */
        data object NavigateToHub : MainEffect
    }
}