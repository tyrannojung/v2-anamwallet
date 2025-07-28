package com.anam145.wallet.feature.identity.data.repository

import com.anam145.wallet.core.security.keystore.DIDKeyManager
import com.anam145.wallet.feature.identity.data.local.DIDLocalDataSource
import com.anam145.wallet.feature.identity.data.remote.DIDApiService
import com.anam145.wallet.feature.identity.domain.model.*
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DIDRepository 구현체
 * DID 관련 비즈니스 로직 구현
 */
@Singleton
class DIDRepositoryImpl @Inject constructor(
    private val apiService: DIDApiService,
    private val localDataSource: DIDLocalDataSource,
    private val keyManager: DIDKeyManager
) : DIDRepository {
    
    companion object {
        private const val USER_KEY_ALIAS = "user_key"
    }
    
    override suspend fun initializeDIDIdentity(userName: String): Result<Unit> {
        return try {
            android.util.Log.d("DIDRepository", "Starting DID identity initialization for: $userName")
            
            // 1. EC 키쌍 생성
            val (publicKeyPem, _) = keyManager.generateAndStoreKeyPair(USER_KEY_ALIAS)
            val publicKeyBase64 = keyManager.pemToBase64(publicKeyPem)
            
            android.util.Log.d("DIDRepository", "Generated public key: ${publicKeyPem.take(50)}...")
            
            // 2. DID 서버 등록
            val request = RegisterDIDRequest(
                publicKeyPem = publicKeyBase64,
                meta = mapOf("name" to userName)
            )
            
            val response = apiService.registerUserDID(request)
            if (!response.isSuccessful) {
                android.util.Log.e("DIDRepository", "DID registration failed: ${response.code()} - ${response.errorBody()?.string()}")
                return Result.failure(
                    Exception("DID 등록 실패: ${response.code()} - ${response.message()}")
                )
            }
            
            val didResponse = response.body()
                ?: return Result.failure(Exception("응답 본문이 비어있습니다"))
            
            android.util.Log.d("DIDRepository", "DID created successfully!")
            android.util.Log.d("DIDRepository", "userId: ${didResponse.userId}")
            android.util.Log.d("DIDRepository", "userDid: ${didResponse.did}")
            
            // 3. 로컬에 DID 자격증명 저장
            localDataSource.saveDIDCredentials(
                userId = didResponse.userId,
                userDid = didResponse.did
            )
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            android.util.Log.e("DIDRepository", "Failed to initialize DID identity", e)
            Result.failure(
                when (e) {
                    is java.net.UnknownHostException -> Exception("네트워크 연결을 확인해주세요")
                    is java.net.SocketTimeoutException -> Exception("서버 응답 시간이 초과되었습니다")
                    else -> e
                }
            )
        }
    }
    
    override suspend fun getDIDCredentials(): DIDCredentials? {
        val publicKey = keyManager.getPublicKey(USER_KEY_ALIAS) ?: return null
        return localDataSource.getDIDCredentials(publicKey)
    }
    
    override suspend fun getDIDDocument(did: String): Result<DIDDocument> {
        return try {
            val response = apiService.getDIDDocument(did)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("DID Document가 비어있습니다"))
            } else {
                Result.failure(
                    Exception("DID 조회 실패: ${response.code()} - ${response.message()}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isDIDInitialized(): Boolean {
        return localDataSource.isDIDInitialized.first()
    }
    
    override suspend fun issueStudentCard(): Result<VerifiableCredential> {
        return try {
            val credentials = getDIDCredentials()
                ?: return Result.failure(Exception("DID가 초기화되지 않았습니다"))
            
            android.util.Log.d("DIDRepository", "Issuing student card for: ${credentials.userDid}")
            
            val request = IssueStudentCardRequest(userDid = credentials.userDid)
            val response = apiService.issueStudentCard(request)
            
            if (!response.isSuccessful) {
                android.util.Log.e("DIDRepository", "Student card issuance failed: ${response.code()} - ${response.errorBody()?.string()}")
                return Result.failure(
                    Exception("학생증 발급 실패: ${response.code()} - ${response.message()}")
                )
            }
            
            val studentResponse = response.body()
                ?: return Result.failure(Exception("응답 본문이 비어있습니다"))
            
            android.util.Log.d("DIDRepository", "Student card issued successfully!")
            android.util.Log.d("DIDRepository", "VC ID: ${studentResponse.vc.id}")
            
            // VC 정보 로컬 저장
            val credentialInfo = IssuedCredential(
                type = CredentialType.STUDENT_CARD,
                vcId = studentResponse.vc.id,
                issuanceDate = studentResponse.vc.issuanceDate,
                studentNumber = (studentResponse.vc.credentialSubject as? CredentialSubject.StudentCard)?.studentNumber,
                university = (studentResponse.vc.credentialSubject as? CredentialSubject.StudentCard)?.university,
                department = (studentResponse.vc.credentialSubject as? CredentialSubject.StudentCard)?.department
            )
            localDataSource.saveStudentCardVC(studentResponse.vc.id, credentialInfo)
            
            Result.success(studentResponse.vc)
            
        } catch (e: Exception) {
            android.util.Log.e("DIDRepository", "Failed to issue student card", e)
            Result.failure(e)
        }
    }
    
    override suspend fun issueDriverLicense(): Result<VerifiableCredential> {
        return try {
            val credentials = getDIDCredentials()
                ?: return Result.failure(Exception("DID가 초기화되지 않았습니다"))
            
            android.util.Log.d("DIDRepository", "Issuing driver license for: ${credentials.userDid}")
            
            val request = IssueDriverLicenseRequest(userDid = credentials.userDid)
            val response = apiService.issueDriverLicense(request)
            
            if (!response.isSuccessful) {
                android.util.Log.e("DIDRepository", "Driver license issuance failed: ${response.code()} - ${response.errorBody()?.string()}")
                return Result.failure(
                    Exception("운전면허증 발급 실패: ${response.code()} - ${response.message()}")
                )
            }
            
            val licenseResponse = response.body()
                ?: return Result.failure(Exception("응답 본문이 비어있습니다"))
            
            android.util.Log.d("DIDRepository", "Driver license issued successfully!")
            android.util.Log.d("DIDRepository", "VC ID: ${licenseResponse.vc.id}")
            
            // VC 정보 로컬 저장
            val credentialInfo = IssuedCredential(
                type = CredentialType.DRIVER_LICENSE,
                vcId = licenseResponse.vc.id,
                issuanceDate = licenseResponse.vc.issuanceDate,
                licenseNumber = licenseResponse.licenseNumber
            )
            localDataSource.saveDriverLicenseVC(licenseResponse.vc.id, credentialInfo)
            
            Result.success(licenseResponse.vc)
            
        } catch (e: Exception) {
            android.util.Log.e("DIDRepository", "Failed to issue driver license", e)
            Result.failure(e)
        }
    }
    
    override fun getIssuedCredentials(): Flow<List<IssuedCredential>> {
        return localDataSource.getIssuedCredentials()
    }
    
    override fun isStudentCardIssued(): Flow<Boolean> {
        return localDataSource.isStudentCardIssued()
    }
    
    override fun isDriverLicenseIssued(): Flow<Boolean> {
        return localDataSource.isDriverLicenseIssued()
    }
}