package com.anam145.wallet.feature.auth.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.security.domain.usecase.VerifyAppPasswordUseCase
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
 * 로그인 화면 뷰모델
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val verifyAppPasswordUseCase: VerifyAppPasswordUseCase,
    private val passwordManager: PasswordManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginContract.State())
    val uiState: StateFlow<LoginContract.State> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<LoginContract.Effect>()
    val effect: SharedFlow<LoginContract.Effect> = _effect.asSharedFlow()
    
    fun handleIntent(intent: LoginContract.Intent) {
        when (intent) {
            is LoginContract.Intent.UpdatePassword -> updatePassword(intent.password)
            LoginContract.Intent.TogglePasswordVisibility -> togglePasswordVisibility()
            LoginContract.Intent.Login -> login()
            LoginContract.Intent.ClearError -> clearError()
        }
    }
    
    private fun updatePassword(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                error = null
            )
        }
    }
    
    private fun togglePasswordVisibility() {
        _uiState.update { state ->
            state.copy(isPasswordVisible = !state.isPasswordVisible)
        }
    }
    
    private fun login() {
        val password = _uiState.value.password
        
        // 비밀번호 검증 (최소 8자)
        if (password.length < 8) {
            _uiState.update { state ->
                state.copy(error = AuthError.PasswordTooShort)
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            verifyAppPasswordUseCase(password).fold(
                onSuccess = { isValid ->
                    if (isValid) {
                        // 비밀번호를 메모리에 캐싱
                        passwordManager.setPassword(password)
                        _effect.emit(LoginContract.Effect.NavigateToMain)
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = AuthError.PasswordMismatch
                            )
                        }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = AuthError.LoginFailed
                        )
                    }
                }
            )
        }
    }
    
    private fun clearError() {
        _uiState.update { state ->
            state.copy(error = null)
        }
    }
}