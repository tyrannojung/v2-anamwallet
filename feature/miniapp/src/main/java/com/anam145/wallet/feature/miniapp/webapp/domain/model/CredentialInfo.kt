package com.anam145.wallet.feature.miniapp.webapp.domain.model

/**
 * 신분증 정보
 * 
 * Main 프로세스에서 가져온 신분증 정보를 나타냅니다.
 */
data class CredentialInfo(
    val id: String,
    val type: CredentialType,
    val name: String,
    val holderName: String,
    val isIssued: Boolean,
    val issuedDate: String? = null,
    val expiryDate: String? = null
)

/**
 * 신분증 타입
 */
enum class CredentialType {
    STUDENT_CARD,
    DRIVER_LICENSE
}