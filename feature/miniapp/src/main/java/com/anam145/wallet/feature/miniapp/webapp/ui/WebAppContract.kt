package com.anam145.wallet.feature.miniapp.webapp.ui

import com.anam145.wallet.core.common.model.MiniAppManifest
import org.json.JSONObject

/**
 * WebApp 상세 화면의 Contract 정의
 * 
 * MVI 패턴에 따라 화면의 상태, 사용자 의도, 부수 효과를 정의합니다.
 */
interface WebAppContract {
    
    /**
     * UI 상태
     * 
     * @property appId 웹앱 ID
     * @property manifest 웹앱 매니페스트
     * @property isLoading 로딩 상태
     * @property isServiceConnected 서비스 연결 상태
     * @property error 에러 메시지
     * @property webViewReady WebView 준비 상태
     * @property activeBlockchainId 활성화된 블록체인 ID
     * @property activeBlockchainName 활성화된 블록체인 이름
     */
    data class State(
        val appId: String = "",
        val manifest: MiniAppManifest? = null,
        val isLoading: Boolean = true,
        val isServiceConnected: Boolean = false,
        val error: String? = null,
        val webViewReady: Boolean = false,
        val activeBlockchainId: String? = null,
        val activeBlockchainName: String? = null,
        val webUrl: String? = null
    )
    
    /**
     * 사용자 의도
     */
    sealed interface Intent {
        /** 결제 요청 */
        data class RequestPayment(val paymentData: JSONObject) : Intent
        
        /** 서비스 재연결 시도 */
        object RetryServiceConnection : Intent
        
        /** 에러 해제 */
        object DismissError : Intent
        
        /** 뒤로가기 */
        object NavigateBack : Intent
    }
    
    /**
     * 부수 효과
     */
    sealed interface Effect {
        /** JavaScript 실행하여 결제 응답 전달 */
        data class SendPaymentResponse(val responseJson: String) : Effect
        
        /** 에러 토스트 표시 */
        data class ShowError(val message: String) : Effect
        
        /** 뒤로가기 네비게이션 */
        object NavigateBack : Effect
    }
}