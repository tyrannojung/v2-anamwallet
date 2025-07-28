package com.anam145.wallet.feature.identity.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.identity.domain.model.CredentialSubject
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DriverLicenseDetailViewModel @Inject constructor(
    private val didRepository: DIDRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val vcId = savedStateHandle.get<String>("vcId") ?: ""
    
    data class DriverLicenseInfo(
        val licenseNumber: String,
        val name: String,
        val birthDate: String,
        val issueDate: String,
        val expiryDate: String,
        val licenseType: String
    )
    
    val driverLicense = flow {
        val vc = didRepository.getDriverLicenseCredential()
        val info = vc?.let {
            val subject = it.credentialSubject as? CredentialSubject.DriverLicense
            DriverLicenseInfo(
                licenseNumber = subject?.licenseNumber ?: "",
                name = subject?.name ?: "",
                birthDate = subject?.birthDate ?: "",
                issueDate = subject?.issueDate ?: "",
                expiryDate = subject?.expiryDate ?: "",
                licenseType = subject?.licenseType ?: ""
            )
        }
        emit(info)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}