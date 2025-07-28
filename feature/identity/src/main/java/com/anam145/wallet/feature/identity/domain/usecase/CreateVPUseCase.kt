package com.anam145.wallet.feature.identity.domain.usecase

import android.util.Log
import com.anam145.wallet.feature.identity.domain.model.VerifiableCredential
import com.anam145.wallet.feature.identity.domain.model.VerifiablePresentation
import com.anam145.wallet.feature.identity.domain.model.Proof
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import com.anam145.wallet.core.security.keystore.DIDKeyManager
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * VP(Verifiable Presentation) 생성 UseCase
 * 
 * 선택된 자격증명으로 VP를 생성하고 서명합니다.
 */
class CreateVPUseCase @Inject constructor(
    private val didRepository: DIDRepository,
    private val keyManager: DIDKeyManager
) {
    
    companion object {
        private const val TAG = "CreateVPUseCase"
        private val gson = Gson()
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    
    /**
     * VP 생성
     * 
     * @param credential 포함할 자격증명
     * @param challenge VP 요청의 challenge
     * @return VP JSON 문자열
     */
    suspend operator fun invoke(
        credential: VerifiableCredential,
        challenge: String
    ): Result<String> {
        return try {
            // 사용자 DID 정보 가져오기
            val didCredentials = didRepository.getDIDCredentials()
                ?: return Result.failure(Exception("DID not initialized"))
            
            // VP 생성
            val vp = VerifiablePresentation(
                context = listOf("https://www.w3.org/ns/credentials/v2"),
                type = listOf("VerifiablePresentation"),
                holder = didCredentials.userDid,
                verifiableCredential = credential,
                proof = null // 서명 전
            )
            
            // VP를 JSON으로 변환 (서명할 데이터)
            val vpWithoutProof = gson.toJson(vp)
            
            // 서명할 데이터: VP JSON + Challenge
            val dataToSign = "$vpWithoutProof$challenge"
            
            // DIDKeyManager에서 개인키로 서명
            val signatureValue = keyManager.signData(
                alias = "user_key",
                data = dataToSign
            )
            
            // Proof 생성
            val proof = Proof(
                type = "Secp256r1Signature2018",
                created = dateFormat.format(Date()),
                verificationMethod = "${didCredentials.userDid}#keys-1",
                proofPurpose = "authentication",
                challenge = challenge,
                proofValue = signatureValue
            )
            
            // 최종 VP 생성
            val finalVP = vp.copy(proof = proof)
            val vpJson = gson.toJson(finalVP)
            
            Result.success(vpJson)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create VP", e)
            Result.failure(e)
        }
    }
    
}