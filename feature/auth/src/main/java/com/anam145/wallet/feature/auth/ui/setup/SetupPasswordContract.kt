package com.anam145.wallet.feature.auth.ui.setup

import com.anam145.wallet.feature.auth.domain.model.AuthError

/**
 * 비밀번호 설정 화면의 UI 상태 및 이벤트 정의
 */
object SetupPasswordContract {
    
    /**
     * UI 상태
     */
    data class State(
        val password: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val passwordError: AuthError? = null,
        val confirmPasswordError: AuthError? = null,
        val error: AuthError? = null,
        val passwordStrength: PasswordStrength = PasswordStrength.NONE
    )
    
    /**
     * 비밀번호 강도
     */
    enum class PasswordStrength {
        NONE,
        WEAK,
        MEDIUM,
        STRONG
    }
    
    /**
     * 사용자 인텐트
     */
    sealed class Intent {
        data class UpdatePassword(val password: String) : Intent()
        data class UpdateConfirmPassword(val confirmPassword: String) : Intent()
        object TogglePasswordVisibility : Intent()
        object ToggleConfirmPasswordVisibility : Intent()
        object SetupPassword : Intent()
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