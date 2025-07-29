package com.anam145.wallet.feature.identity.ui.issue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.identity.domain.usecase.GetCredentialStatusUseCase
import com.anam145.wallet.feature.identity.domain.usecase.IssueDriverLicenseUseCase
import com.anam145.wallet.feature.identity.domain.usecase.IssueStudentCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 신분증 발급 선택 화면 ViewModel
 */
@HiltViewModel
class IssueSelectViewModel @Inject constructor(
    private val issueStudentCardUseCase: IssueStudentCardUseCase,
    private val issueDriverLicenseUseCase: IssueDriverLicenseUseCase,
    private val getCredentialStatusUseCase: GetCredentialStatusUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IssueSelectContract.State())
    val uiState: StateFlow<IssueSelectContract.State> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<IssueSelectContract.Effect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<IssueSelectContract.Effect> = _effect.asSharedFlow()
    
    init {
        observeCredentialStatus()
    }
    
    fun handleIntent(intent: IssueSelectContract.Intent) {
        when (intent) {
            IssueSelectContract.Intent.IssueStudentCard -> issueStudentCard()
            IssueSelectContract.Intent.IssueDriverLicense -> issueDriverLicense()
            IssueSelectContract.Intent.ClearError -> clearError()
            IssueSelectContract.Intent.NavigateBack -> navigateBack()
        }
    }
    
    private fun observeCredentialStatus() {
        viewModelScope.launch {
            getCredentialStatusUseCase().collect { status ->
                _uiState.update { state ->
                    state.copy(
                        isStudentCardIssued = status.isStudentCardIssued,
                        isDriverLicenseIssued = status.isDriverLicenseIssued
                    )
                }
            }
        }
    }
    
    private fun issueStudentCard() {
        if (_uiState.value.isStudentCardIssued || _uiState.value.isIssuingStudentCard) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isIssuingStudentCard = true, error = null) }
            
            issueStudentCardUseCase()
                .onSuccess { vc ->
                    android.util.Log.d("IssueSelectViewModel", "Student card issued successfully: ${vc.id}")
                    _uiState.update { it.copy(isIssuingStudentCard = false) }
                    _effect.emit(IssueSelectContract.Effect.ShowToast("학생증이 발급되었습니다"))
                }
                .onFailure { error ->
                    android.util.Log.e("IssueSelectViewModel", "Failed to issue student card", error)
                    _uiState.update { 
                        it.copy(
                            isIssuingStudentCard = false,
                            error = error.message ?: "학생증 발급에 실패했습니다"
                        )
                    }
                }
        }
    }
    
    private fun issueDriverLicense() {
        if (_uiState.value.isDriverLicenseIssued || _uiState.value.isIssuingDriverLicense) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isIssuingDriverLicense = true, error = null) }
            
            issueDriverLicenseUseCase()
                .onSuccess { vc ->
                    android.util.Log.d("IssueSelectViewModel", "Driver license issued successfully: ${vc.id}")
                    _uiState.update { it.copy(isIssuingDriverLicense = false) }
                    _effect.emit(IssueSelectContract.Effect.ShowToast("운전면허증이 발급되었습니다"))
                }
                .onFailure { error ->
                    android.util.Log.e("IssueSelectViewModel", "Failed to issue driver license", error)
                    _uiState.update { 
                        it.copy(
                            isIssuingDriverLicense = false,
                            error = error.message ?: "운전면허증 발급에 실패했습니다"
                        )
                    }
                }
        }
    }
    
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(IssueSelectContract.Effect.NavigateBack)
        }
    }
}