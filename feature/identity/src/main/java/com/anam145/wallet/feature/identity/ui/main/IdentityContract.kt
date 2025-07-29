package com.anam145.wallet.feature.identity.ui.main

import com.anam145.wallet.feature.identity.domain.model.IssuedCredential

/**
 * Identity 메인 화면 Contract
 */
interface IdentityContract {
    
    data class State(
        val issuedCredentials: List<IssuedCredential> = emptyList(),
        val isLoading: Boolean = false
    )
    
    sealed interface Intent {
        data class NavigateToDetail(val credential: IssuedCredential) : Intent
        data object NavigateToIssue : Intent
    }
    
    sealed interface Effect {
        data class NavigateToStudentCardDetail(val vcId: String) : Effect
        data class NavigateToDriverLicenseDetail(val vcId: String) : Effect
        data object NavigateToIssue : Effect
    }
}