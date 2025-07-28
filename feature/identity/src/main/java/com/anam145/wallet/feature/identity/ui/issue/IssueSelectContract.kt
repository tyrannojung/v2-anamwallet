package com.anam145.wallet.feature.identity.ui.issue

/**
 * 신분증 발급 선택 화면 Contract
 */
interface IssueSelectContract {
    
    data class State(
        val isStudentCardIssued: Boolean = false,
        val isDriverLicenseIssued: Boolean = false,
        val isIssuingStudentCard: Boolean = false,
        val isIssuingDriverLicense: Boolean = false,
        val error: String? = null
    )
    
    sealed interface Intent {
        data object IssueStudentCard : Intent
        data object IssueDriverLicense : Intent
        data object ClearError : Intent
        data object NavigateBack : Intent
    }
    
    sealed interface Effect {
        data object NavigateBack : Effect
        data class ShowToast(val message: String) : Effect
    }
}