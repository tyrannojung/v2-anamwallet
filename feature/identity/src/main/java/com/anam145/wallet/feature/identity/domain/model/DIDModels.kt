package com.anam145.wallet.feature.identity.domain.model

import com.google.gson.annotations.SerializedName

/**
 * DID 관련 도메인 모델
 */

// API Request/Response Models
data class RegisterDIDRequest(
    @SerializedName("publicKeyPem") val publicKeyPem: String,
    @SerializedName("meta") val meta: Map<String, String> = emptyMap()
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