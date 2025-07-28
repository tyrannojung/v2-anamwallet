package com.anam145.wallet.feature.identity.data.remote

import com.anam145.wallet.feature.identity.domain.model.CredentialSubject
import com.google.gson.*
import java.lang.reflect.Type

/**
 * CredentialSubject를 위한 커스텀 Deserializer
 * VC 타입에 따라 적절한 CredentialSubject 구현체로 변환
 */
class CredentialSubjectDeserializer : JsonDeserializer<CredentialSubject> {
    override fun deserialize(
        json: JsonElement?, 
        typeOfT: Type?, 
        context: JsonDeserializationContext?
    ): CredentialSubject {
        val jsonObject = json?.asJsonObject 
            ?: throw JsonParseException("Expected JsonObject for CredentialSubject")
        
        // 디버깅을 위한 로그
        android.util.Log.d("CredentialSubjectDeserializer", "Deserializing: $jsonObject")
        
        return when {
            jsonObject.has("studentId") && jsonObject.has("studentNumber") -> {
                // StudentCard 타입
                CredentialSubject.StudentCard(
                    studentId = jsonObject.get("studentId").asString,
                    studentNumber = jsonObject.get("studentNumber").asString,
                    name = jsonObject.get("name")?.asString ?: "",
                    university = jsonObject.get("university")?.asString ?: "",
                    department = jsonObject.get("department")?.asString ?: ""
                )
            }
            jsonObject.has("licenseId") -> {
                // DriverLicense 타입
                CredentialSubject.DriverLicense(
                    licenseId = jsonObject.get("licenseId").asString,
                    licenseNumber = jsonObject.get("licenseNumber")?.asString ?: "",
                    name = jsonObject.get("name")?.asString ?: "",
                    birthDate = jsonObject.get("birthDate")?.asString ?: "",
                    issueDate = jsonObject.get("licenseIssueDate")?.asString 
                        ?: jsonObject.get("issueDate")?.asString ?: "",
                    expiryDate = jsonObject.get("licenseExpiryDate")?.asString 
                        ?: jsonObject.get("expiryDate")?.asString ?: "",
                    licenseType = jsonObject.get("licenseType")?.asString ?: ""
                )
            }
            else -> {
                throw JsonParseException("Unknown CredentialSubject type: $jsonObject")
            }
        }
    }
}