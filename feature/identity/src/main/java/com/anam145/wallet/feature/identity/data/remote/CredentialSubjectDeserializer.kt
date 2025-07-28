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
        
        return when {
            jsonObject.has("studentId") && jsonObject.has("studentNumber") -> {
                // StudentCard 타입
                CredentialSubject.StudentCard(
                    studentId = jsonObject.get("studentId").asString,
                    studentNumber = jsonObject.get("studentNumber").asString,
                    university = jsonObject.get("university").asString,
                    department = jsonObject.get("department").asString
                )
            }
            jsonObject.has("licenseId") -> {
                // DriverLicense 타입
                CredentialSubject.DriverLicense(
                    licenseId = jsonObject.get("licenseId").asString
                )
            }
            else -> {
                throw JsonParseException("Unknown CredentialSubject type")
            }
        }
    }
}