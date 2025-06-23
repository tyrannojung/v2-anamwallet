package com.anam145.wallet.feature.miniapp.ui

import com.anam145.wallet.core.common.model.MiniApp

/**
 * MiniApp 화면의 Contract 정의
 * 
 * MVI (Model-View-Intent) 패턴을 사용하여 화면의 상태, 사용자 의도, 부수 효과를 정의합니다.
 * 이를 통해 화면의 모든 동작을 한 곳에서 관리하고 테스트하기 쉽게 만듭니다.
 */
interface MiniAppContract {
    
    /**
     * UI 상태 - 화면에 표시되는 데이터
     */
    data class MiniAppState(
        val isLoading: Boolean = false,
        val miniApp: MiniApp? = null,
        val webViewUrl: String? = null,
        val canGoBack: Boolean = false,
        val canGoForward: Boolean = false,
        val progress: Int = 0,
        val error: String? = null
    )
    
    /**
     * 사용자 의도 - 사용자의 액션
     */
    sealed interface MiniAppIntent {
        /** 미니앱 로드 */
        data class LoadMiniApp(val appId: String) : MiniAppIntent
        
        /** 웹뷰 네비게이션 */
        data object NavigateBack : MiniAppIntent
        data object NavigateForward : MiniAppIntent
        data object Refresh : MiniAppIntent
        
        /** 웹뷰 상태 업데이트 */
        data class UpdateProgress(val progress: Int) : MiniAppIntent
        data class UpdateNavigationState(val canGoBack: Boolean, val canGoForward: Boolean) : MiniAppIntent
        
        /** JavaScript 브리지 이벤트 */
        data class HandleJavaScriptMessage(val message: String) : MiniAppIntent
        
        /** 에러 처리 */
        data class ShowError(val error: String) : MiniAppIntent
        data object DismissError : MiniAppIntent
        
        /** 네비게이션 */
        data object ClickClose : MiniAppIntent
        data object ClickMore : MiniAppIntent
    }
    
    /**
     * 부수 효과 - 시스템 레벨의 일회성 동작
     */
    sealed interface MiniAppEffect {
        /** 네비게이션 효과 */
        data object NavigateBack : MiniAppEffect
        data object ShowMoreOptions : MiniAppEffect
        
        /** JavaScript 실행 */
        data class ExecuteJavaScript(val script: String) : MiniAppEffect
        
        /** 네이티브 API 호출 */
        data class CallNativeAPI(val apiName: String, val params: Map<String, Any>) : MiniAppEffect
        
        /** 토스트 메시지 */
        data class ShowToast(val message: String) : MiniAppEffect
    }
}