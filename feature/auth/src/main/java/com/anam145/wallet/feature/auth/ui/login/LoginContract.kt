package com.anam145.wallet.feature.auth.ui.login

import com.anam145.wallet.feature.auth.domain.model.AuthError

/**
 * 로그인 화면의 UI 상태 및 이벤트 정의
 */
object LoginContract {
    
    /**
     * UI 상태
     */
    data class State(
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val error: AuthError? = null
    )
    
    /**
     * 사용자 인텐트
     */
    sealed class Intent {
        data class UpdatePassword(val password: String) : Intent()
        object TogglePasswordVisibility : Intent()
        object Login : Intent()
        object ClearError : Intent()
    }
    
    /**
     * 부수 효과
     */
    sealed class Effect {
        object NavigateToMain : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}