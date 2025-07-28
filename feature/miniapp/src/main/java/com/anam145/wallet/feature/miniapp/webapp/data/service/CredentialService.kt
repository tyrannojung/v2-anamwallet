package com.anam145.wallet.feature.miniapp.webapp.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.anam145.wallet.feature.miniapp.ICredentialService
import com.anam145.wallet.feature.miniapp.IVPCallback
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import com.anam145.wallet.feature.identity.domain.usecase.CreateVPUseCase
import com.anam145.wallet.feature.miniapp.webapp.domain.model.CredentialInfo
import com.anam145.wallet.feature.miniapp.webapp.domain.model.CredentialType
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Main 프로세스에서 실행되는 신분증 서비스
 * 
 * WebApp 프로세스에서 AIDL을 통해 신분증 정보를 조회하고 VP를 생성합니다.
 */
@AndroidEntryPoint
class CredentialService : Service() {
    
    companion object {
        private const val TAG = "CredentialService"
    }
    
    @Inject
    lateinit var didRepository: DIDRepository
    
    @Inject
    lateinit var createVPUseCase: CreateVPUseCase
    
    private val gson = Gson()
    
    private val binder = object : ICredentialService.Stub() {
        
        override fun getCredentials(): String {
            Log.d(TAG, "getCredentials called")
            
            return try {
                // DIDRepository에서 실제 신분증 정보 조회
                val credentials = runBlocking {
                    val issuedCredentials = didRepository.getIssuedCredentials().first()
                    val didCredentials = didRepository.getDIDCredentials()
                    val userName = didCredentials?.userDid?.substringAfterLast(":") ?: "사용자"
                    
                    val credentialList = mutableListOf<CredentialInfo>()
                    
                    // 학생증 정보
                    val studentCardIssued = didRepository.isStudentCardIssued().first()
                    val studentInfo = issuedCredentials.find { it.type == com.anam145.wallet.feature.identity.domain.model.CredentialType.STUDENT_CARD }
                    
                    credentialList.add(
                        CredentialInfo(
                            id = "student_001",
                            type = CredentialType.STUDENT_CARD,
                            name = "고려대학교 학생증",
                            holderName = userName,
                            isIssued = studentCardIssued,
                            issuedDate = studentInfo?.issuanceDate
                        )
                    )
                    
                    // 운전면허증 정보
                    val driverLicenseIssued = didRepository.isDriverLicenseIssued().first()
                    val driverInfo = issuedCredentials.find { it.type == com.anam145.wallet.feature.identity.domain.model.CredentialType.DRIVER_LICENSE }
                    
                    credentialList.add(
                        CredentialInfo(
                            id = "driver_001",
                            type = CredentialType.DRIVER_LICENSE,
                            name = "대한민국 운전면허증",
                            holderName = userName,
                            isIssued = driverLicenseIssued,
                            issuedDate = driverInfo?.issuanceDate
                        )
                    )
                    
                    credentialList
                }
                
                gson.toJson(credentials)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting credentials", e)
                "[]"
            }
        }
        
        override fun createVP(credentialId: String, challenge: String, callback: IVPCallback) {
            Log.d(TAG, "createVP called - credentialId: $credentialId, challenge: $challenge")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 선택된 신분증으로 VP 생성
                    val credential = when (credentialId) {
                        "student_001" -> didRepository.getStudentCredential()
                        "driver_001" -> didRepository.getDriverLicenseCredential()
                        else -> null
                    }
                    
                    if (credential == null) {
                        callback.onError("Credential not found")
                        return@launch
                    }
                    
                    // VP 생성
                    val result = createVPUseCase(credential, challenge)
                    
                    result.fold(
                        onSuccess = { vpJson ->
                            Log.d(TAG, "VP created successfully")
                            callback.onSuccess(vpJson)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "VP creation failed", error)
                            callback.onError(error.message ?: "VP creation failed")
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating VP", e)
                    callback.onError(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "CredentialService bound")
        return binder
    }
    
    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "CredentialService unbound")
        return super.onUnbind(intent)
    }
}