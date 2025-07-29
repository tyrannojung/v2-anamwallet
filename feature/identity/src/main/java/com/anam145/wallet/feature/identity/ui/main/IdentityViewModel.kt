package com.anam145.wallet.feature.identity.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.identity.domain.model.CredentialType
import com.anam145.wallet.feature.identity.domain.model.IssuedCredential
import com.anam145.wallet.feature.identity.domain.usecase.GetIssuedCredentialsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Identity 메인 화면 ViewModel
 */
@HiltViewModel
class IdentityViewModel @Inject constructor(
    private val getIssuedCredentialsUseCase: GetIssuedCredentialsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IdentityContract.State())
    val uiState: StateFlow<IdentityContract.State> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<IdentityContract.Effect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<IdentityContract.Effect> = _effect.asSharedFlow()
    
    init {
        loadIssuedCredentials()
    }
    
    fun handleIntent(intent: IdentityContract.Intent) {
        when (intent) {
            is IdentityContract.Intent.NavigateToDetail -> navigateToDetail(intent.credential)
            IdentityContract.Intent.NavigateToIssue -> navigateToIssue()
        }
    }
    
    private fun loadIssuedCredentials() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getIssuedCredentialsUseCase().collect { credentials ->
                _uiState.update { state ->
                    state.copy(
                        issuedCredentials = credentials,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun navigateToDetail(credential: IssuedCredential) {
        viewModelScope.launch {
            val effect = when (credential.type) {
                CredentialType.STUDENT_CARD -> 
                    IdentityContract.Effect.NavigateToStudentCardDetail(credential.vcId)
                CredentialType.DRIVER_LICENSE -> 
                    IdentityContract.Effect.NavigateToDriverLicenseDetail(credential.vcId)
            }
            _effect.emit(effect)
        }
    }
    
    private fun navigateToIssue() {
        viewModelScope.launch {
            _effect.emit(IdentityContract.Effect.NavigateToIssue)
        }
    }
}