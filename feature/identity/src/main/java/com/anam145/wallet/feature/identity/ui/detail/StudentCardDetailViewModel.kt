package com.anam145.wallet.feature.identity.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.identity.domain.model.CredentialSubject
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudentCardDetailViewModel @Inject constructor(
    private val didRepository: DIDRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val vcId = savedStateHandle.get<String>("vcId") ?: ""
    
    data class StudentCardInfo(
        val studentNumber: String,
        val name: String,
        val university: String,
        val department: String,
        val issuanceDate: String
    )
    
    val studentCard = flow {
        val vc = didRepository.getStudentCredential()
        val info = vc?.let {
            val subject = it.credentialSubject as? CredentialSubject.StudentCard
            StudentCardInfo(
                studentNumber = subject?.studentNumber ?: "",
                name = subject?.name ?: "",
                university = subject?.university ?: "",
                department = subject?.department ?: "",
                issuanceDate = it.issuanceDate
            )
        }
        emit(info)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}