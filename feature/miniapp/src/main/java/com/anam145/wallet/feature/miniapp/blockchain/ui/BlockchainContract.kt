package com.anam145.wallet.feature.miniapp.blockchain.ui

import com.anam145.wallet.core.common.model.MiniAppManifest

/**
 * 블록체인 UI 화면의 Contract 정의
 * 
 * MVI 패턴에 따라 화면의 상태, 사용자 의도, 부수 효과를 정의합니다.
 */
interface BlockchainContract {
    
    /**
     * UI 상태
     * 
     * @property blockchainId 블록체인 ID
     * @property manifest 블록체인 매니페스트
     * @property isLoading 로딩 상태
     * @property isServiceConnected 서비스 연결 상태
     * @property error 에러 메시지
     * @property webViewReady WebView 준비 상태
     * @property isActivated 블록체인 활성화 상태
     */
    data class State(
        val blockchainId: String = "",
        val manifest: MiniAppManifest? = null,
        val isLoading: Boolean = true,
        val isServiceConnected: Boolean = false,
        val error: String? = null,
        val webViewReady: Boolean = false,
        val isActivated: Boolean = false
    )
    
    /**
     * 사용자 의도
     */
    sealed interface Intent {
        /** 블록체인 UI 로드 */
        data class LoadBlockchain(val blockchainId: String) : Intent
        
        /** 서비스 재연결 시도 */
        object RetryServiceConnection : Intent
        
        /** 에러 해제 */
        object DismissError : Intent
        
        /** 뒤로가기 */
        object NavigateBack : Intent
        
        /** WebView 준비 완료 */
        object WebViewReady : Intent
    }
    
    /**
     * 부수 효과
     */
    sealed interface Effect {
        /** 에러 토스트 표시 */
        data class ShowError(val message: String) : Effect
        
        /** 뒤로가기 네비게이션 */
        object NavigateBack : Effect
        
        /** WebView에 URL 로드 */
        data class LoadUrl(val url: String) : Effect
    }
}