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
        val isLoading: Boolean = false,          // 앱 목록을 로드 중일 때 true
        val isSyncing: Boolean = false,          // 첫 실행 시 MiniApp 초기화 중일 때 true
        val blockchainApps: List<MiniApp> = emptyList(),     // 블록체인 타입 앱 목록
        val regularApps: List<MiniApp> = emptyList(),        // 일반 앱 목록
        val activeBlockchainId: String? = null,              // 현재 활성화된 블록체인 앱 ID
        val error: String? = null                            // 에러 메시지 (null이면 에러 없음)
    )
    
    /**
     * 사용자 의도 - 사용자의 액션
     */
    sealed interface MainIntent {
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
        /** 블록체인 액티비티 실행 (blockchain 프로세스) */
        data class LaunchBlockchainActivity(val blockchainId: String) : MainEffect
        
        /** 웹앱 액티비티 실행 (app 프로세스) */
        data class LaunchWebAppActivity(val appId: String) : MainEffect
        
        /** Hub 화면으로 이동 (Navigation Component) */
        data object NavigateToHub : MainEffect
        
        /** 에러 메시지 표시 */
        data class ShowError(val message: String) : MainEffect
    }
}