package com.anam145.wallet.feature.identity.domain.model

import com.google.gson.annotations.SerializedName

/**
 * DID 관련 도메인 모델
 */

// API Request/Response Models
data class RegisterDIDRequest(
    @SerializedName("publicKey") val publicKey: String,
    @SerializedName("additionalInfo") val additionalInfo: Map<String, String> = emptyMap()
)

data class RegisterDIDResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("did") val did: String
)

// DID Document
data class DIDDocument(
    @SerializedName("@context") val context: String,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("created") val created: String,
    @SerializedName("updated") val updated: String,
    @SerializedName("controller") val controller: String,
    @SerializedName("verificationMethod") val verificationMethod: List<VerificationMethod>,
    @SerializedName("authentication") val authentication: List<String>,
    @SerializedName("additionalInfo") val additionalInfo: Map<String, String>? = null,
    @SerializedName("status") val status: String
)

data class VerificationMethod(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("controller") val controller: String,
    @SerializedName("publicKeyPem") val publicKeyPem: String
)

// Local Storage Models
data class DIDCredentials(
    val userId: String,      // 사용자 고유 ID
    val userDid: String,     // DID (did:anam145:user:xxx)
    val publicKey: String    // 공개키 (PEM 형식)
)

// VC Request Models
data class IssueStudentCardRequest(
    @SerializedName("userDid") val userDid: String
)

data class IssueDriverLicenseRequest(
    @SerializedName("userDid") val userDid: String
)

// VC Response Models
data class StudentCardResponse(
    @SerializedName("studentDid") val studentDid: String,
    @SerializedName("userDid") val userDid: String,
    @SerializedName("vc") val vc: VerifiableCredential
)

data class DriverLicenseResponse(
    @SerializedName("licenseDid") val licenseDid: String,
    @SerializedName("userDid") val userDid: String,
    @SerializedName("licenseNumber") val licenseNumber: String,
    @SerializedName("vc") val vc: VerifiableCredential
)

// VC Models
data class VerifiableCredential(
    @SerializedName("@context") val context: List<String>,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: List<String>,
    @SerializedName("issuer") val issuer: Issuer,
    @SerializedName("issuanceDate") val issuanceDate: String,
    @SerializedName("credentialSubject") val credentialSubject: CredentialSubject,
    @SerializedName("proof") val proof: Proof
)

data class Issuer(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

// CredentialSubject를 sealed class로 변경하여 다양한 타입 지원
sealed class CredentialSubject {
    data class StudentCard(
        @SerializedName("studentId") val studentId: String,
        @SerializedName("studentNumber") val studentNumber: String,
        @SerializedName("name") val name: String,
        @SerializedName("university") val university: String,
        @SerializedName("department") val department: String
    ) : CredentialSubject()
    
    data class DriverLicense(
        @SerializedName("licenseId") val licenseId: String,
        @SerializedName("licenseNumber") val licenseNumber: String,
        @SerializedName("name") val name: String,
        @SerializedName("birthDate") val birthDate: String,
        @SerializedName("issueDate") val issueDate: String,
        @SerializedName("expiryDate") val expiryDate: String,
        @SerializedName("licenseType") val licenseType: String
    ) : CredentialSubject()
}

data class Proof(
    @SerializedName("type") val type: String,
    @SerializedName("created") val created: String,
    @SerializedName("proofPurpose") val proofPurpose: String,
    @SerializedName("verificationMethod") val verificationMethod: String,
    @SerializedName("proofValue") val proofValue: String,
    @SerializedName("challenge") val challenge: String? = null
)

// Credential Type
enum class CredentialType {
    STUDENT_CARD,
    DRIVER_LICENSE
}

// VP (Verifiable Presentation) Model
data class VerifiablePresentation(
    @SerializedName("@context") val context: List<String>,
    @SerializedName("type") val type: List<String>,
    @SerializedName("holder") val holder: String,
    @SerializedName("verifiableCredential") val verifiableCredential: VerifiableCredential,
    @SerializedName("proof") val proof: Proof?
)

// Local Credential Info
data class IssuedCredential(
    val type: CredentialType,
    val vcId: String,
    val issuanceDate: String,
    val name: String? = null,
    val studentNumber: String? = null,
    val licenseNumber: String? = null,
    val university: String? = null,
    val department: String? = null
)