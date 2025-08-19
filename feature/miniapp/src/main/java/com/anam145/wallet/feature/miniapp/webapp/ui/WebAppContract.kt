package com.anam145.wallet.feature.miniapp.webapp.ui

import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.miniapp.webapp.domain.model.CredentialInfo
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
     * @property connectionTimeout 서비스 연결 타임아웃 (10초) 여부
     * @property error 에러 메시지
     * @property webViewReady WebView 준비 상태
     * @property activeBlockchainId 활성화된 블록체인 ID
     * @property activeBlockchainName 활성화된 블록체인 이름
     * @property showVPBottomSheet VP 바텀시트 표시 여부
     * @property vpRequestData VP 요청 데이터
     * @property credentials 신분증 목록
     * @property showTransactionApproval 트랜잭션 승인 바텀시트 표시 여부
     * @property pendingTransactionJson 대기 중인 트랜잭션 JSON
     */
    data class State(
        val appId: String = "",
        val manifest: MiniAppManifest? = null,
        val isLoading: Boolean = true,
        val isServiceConnected: Boolean = false,
        val connectionTimeout: Boolean = false,
        val error: String? = null,
        val webViewReady: Boolean = false,
        val activeBlockchainId: String? = null,
        val activeBlockchainName: String? = null,
        val webUrl: String? = null,
        val showVPBottomSheet: Boolean = false,
        val vpRequestData: VPRequestData? = null,
        val credentials: List<CredentialInfo> = emptyList(),
        val showTransactionApproval: Boolean = false,
        val pendingTransactionJson: String? = null
    )
    
    /**
     * VP 요청 데이터
     */
    data class VPRequestData(
        val service: String,
        val purpose: String,
        val challenge: String,
        val type: String = "both" // "both", "driver", "student"
    )
    
    /**
     * 사용자 의도
     */
    sealed interface Intent {
        /** 결제 요청 */
        data class RequestTransaction(val transactionData: JSONObject) : Intent
        
        /** VP 요청 */
        data class RequestVP(val vpRequest: JSONObject) : Intent
        
        /** VP 바텀시트 닫기 */
        object DismissVPBottomSheet : Intent
        
        /** 신분증 선택 */
        data class SelectCredential(val credentialId: String) : Intent
        
        /** 트랜잭션 승인 */
        object ApproveTransaction : Intent
        
        /** 트랜잭션 거절 */
        object RejectTransaction : Intent
        
        /** 트랜잭션 승인 바텀시트 닫기 */
        object DismissTransactionApproval : Intent
        
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
        data class SendTransactionResponse(val responseJson: String) : Effect
        
        /** JavaScript 실행하여 트랜잭션 에러 전달 */
        data class SendTransactionError(val error: String) : Effect
        
        /** JavaScript 실행하여 VP 응답 전달 */
        data class SendVPResponse(val vpJson: String) : Effect
        
        /** JavaScript 실행하여 VP 에러 전달 */
        data class SendVPError(val error: String) : Effect
        
        /** 에러 토스트 표시 */
        data class ShowError(val message: String) : Effect
        
        /** 뒤로가기 네비게이션 */
        object NavigateBack : Effect
    }
}