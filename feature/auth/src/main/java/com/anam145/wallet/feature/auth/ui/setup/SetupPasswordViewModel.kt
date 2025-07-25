package com.anam145.wallet.feature.auth.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.security.domain.usecase.SaveAppPasswordUseCase
import com.anam145.wallet.feature.auth.domain.PasswordManager
import com.anam145.wallet.feature.auth.domain.model.AuthError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 비밀번호 설정 화면 뷰모델
 */
@HiltViewModel
class SetupPasswordViewModel @Inject constructor(
    private val saveAppPasswordUseCase: SaveAppPasswordUseCase,
    private val passwordManager: PasswordManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SetupPasswordContract.State())
    val uiState: StateFlow<SetupPasswordContract.State> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<SetupPasswordContract.Effect>()
    val effect: SharedFlow<SetupPasswordContract.Effect> = _effect.asSharedFlow()
    
    fun handleIntent(intent: SetupPasswordContract.Intent) {
        when (intent) {
            is SetupPasswordContract.Intent.UpdatePassword -> updatePassword(intent.password)
            is SetupPasswordContract.Intent.UpdateConfirmPassword -> updateConfirmPassword(intent.confirmPassword)
            SetupPasswordContract.Intent.TogglePasswordVisibility -> togglePasswordVisibility()
            SetupPasswordContract.Intent.ToggleConfirmPasswordVisibility -> toggleConfirmPasswordVisibility()
            SetupPasswordContract.Intent.SetupPassword -> setupPassword()
            SetupPasswordContract.Intent.ClearError -> clearError()
        }
    }
    
    private fun updatePassword(password: String) {
        val strength = calculatePasswordStrength(password)
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordStrength = strength,
                passwordError = null,
                error = null
            )
        }
    }
    
    private fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = null,
                error = null
            )
        }
    }
    
    private fun togglePasswordVisibility() {
        _uiState.update { state ->
            state.copy(isPasswordVisible = !state.isPasswordVisible)
        }
    }
    
    private fun toggleConfirmPasswordVisibility() {
        _uiState.update { state ->
            state.copy(isConfirmPasswordVisible = !state.isConfirmPasswordVisible)
        }
    }
    
    private fun setupPassword() {
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        
        // 유효성 검증
        var hasError = false
        
        if (password.length < 8) {
            _uiState.update { state ->
                state.copy(passwordError = AuthError.PasswordTooShort)
            }
            hasError = true
        }
        
        if (password != confirmPassword) {
            _uiState.update { state ->
                state.copy(confirmPasswordError = AuthError.PasswordMismatch)
            }
            hasError = true
        }
        
        if (hasError) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            saveAppPasswordUseCase(password).fold(
                onSuccess = {
                    // 비밀번호를 메모리에 캐싱
                    passwordManager.setPassword(password)
                    _effect.emit(SetupPasswordContract.Effect.NavigateToMain)
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = AuthError.PasswordSetupFailed
                        )
                    }
                }
            )
        }
    }
    
    private fun clearError() {
        _uiState.update { state ->
            state.copy(
                error = null,
                passwordError = null,
                confirmPasswordError = null
            )
        }
    }
    
    private fun calculatePasswordStrength(password: String): SetupPasswordContract.PasswordStrength {
        return when {
            password.isEmpty() -> SetupPasswordContract.PasswordStrength.NONE
            password.length < 8 -> SetupPasswordContract.PasswordStrength.WEAK
            password.length < 12 && containsVariety(password) -> SetupPasswordContract.PasswordStrength.MEDIUM
            password.length >= 12 && containsVariety(password) -> SetupPasswordContract.PasswordStrength.STRONG
            else -> SetupPasswordContract.PasswordStrength.MEDIUM
        }
    }
    
    private fun containsVariety(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        return listOf(hasUpperCase, hasLowerCase, hasDigit, hasSpecialChar).count { it } >= 3
    }
}