package com.anam145.wallet.feature.identity.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anam145.wallet.feature.identity.domain.model.*
import com.anam145.wallet.feature.identity.data.remote.CredentialSubjectDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.didDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "did_preferences"
)

/**
 * DID 관련 로컬 데이터 저장소
 * DID 자격증명 정보를 DataStore로 안전하게 관리
 */
@Singleton
class DIDLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_USER_ID = stringPreferencesKey("userId")
        private val KEY_USER_DID = stringPreferencesKey("userDid")
        private val KEY_IS_INITIALIZED = booleanPreferencesKey("isInitialized")
        
        // 전체 VC 저장용 키
        private val KEY_STUDENT_VC = stringPreferencesKey("studentVc")
        private val KEY_DRIVER_LICENSE_VC = stringPreferencesKey("driverLicenseVc")
    }
    
    private val dataStore = context.didDataStore
    private val gson = GsonBuilder()
        .registerTypeAdapter(CredentialSubject::class.java, CredentialSubjectDeserializer())
        .create()
    
    /**
     * DID 자격증명 정보 저장 (공개키는 DIDKeyManager에서 관리)
     */
    suspend fun saveDIDCredentials(userId: String, userDid: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USER_DID] = userDid
            preferences[KEY_IS_INITIALIZED] = true
        }
    }
    
    /**
     * DID 자격증명 조회
     */
    suspend fun getDIDCredentials(publicKey: String): DIDCredentials? {
        val preferences = dataStore.data.first()
        
        val userId = preferences[KEY_USER_ID] ?: return null
        val userDid = preferences[KEY_USER_DID] ?: return null
        
        return DIDCredentials(
            userId = userId,
            userDid = userDid,
            publicKey = publicKey
        )
    }
    
    /**
     * User DID 조회
     */
    fun getUserDid(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_USER_DID]
    }
    
    /**
     * DID 초기화 상태
     */
    val isDIDInitialized: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_INITIALIZED] ?: false
    }
    
    
    /**
     * 발급된 VC 목록 조회 (전체 VC에서 추출)
     */
    fun getIssuedCredentials(): Flow<List<IssuedCredential>> = dataStore.data.map { preferences ->
        val credentials = mutableListOf<IssuedCredential>()
        
        // 학생증 VC에서 정보 추출
        preferences[KEY_STUDENT_VC]?.let { vcJson ->
            try {
                android.util.Log.d("DIDLocalDataSource", "Student VC JSON: $vcJson")
                val vc = gson.fromJson(vcJson, VerifiableCredential::class.java)
                android.util.Log.d("DIDLocalDataSource", "Parsed VC: $vc")
                val subject = vc.credentialSubject as? CredentialSubject.StudentCard
                android.util.Log.d("DIDLocalDataSource", "Student subject: $subject")
                if (subject != null) {
                    credentials.add(
                        IssuedCredential(
                            type = CredentialType.STUDENT_CARD,
                            vcId = vc.id,
                            issuanceDate = vc.issuanceDate,
                            studentNumber = subject.studentNumber,
                            university = subject.university,
                            department = subject.department
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DIDLocalDataSource", "Failed to decode student VC", e)
            }
        }
        
        // 운전면허증 VC에서 정보 추출
        preferences[KEY_DRIVER_LICENSE_VC]?.let { vcJson ->
            try {
                android.util.Log.d("DIDLocalDataSource", "Driver License VC JSON: $vcJson")
                val vc = gson.fromJson(vcJson, VerifiableCredential::class.java)
                android.util.Log.d("DIDLocalDataSource", "Parsed VC: $vc")
                val subject = vc.credentialSubject as? CredentialSubject.DriverLicense
                android.util.Log.d("DIDLocalDataSource", "Driver License subject: $subject")
                if (subject != null) {
                    credentials.add(
                        IssuedCredential(
                            type = CredentialType.DRIVER_LICENSE,
                            vcId = vc.id,
                            issuanceDate = vc.issuanceDate,
                            licenseNumber = subject.licenseNumber
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("DIDLocalDataSource", "Failed to decode driver license VC", e)
            }
        }
        
        credentials
    }
    
    /**
     * 학생증 발급 여부
     */
    fun isStudentCardIssued(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_STUDENT_VC] != null
    }
    
    /**
     * 운전면허증 발급 여부
     */
    fun isDriverLicenseIssued(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DRIVER_LICENSE_VC] != null
    }
    
    
    /**
     * 학생증 전체 VC 저장
     */
    suspend fun saveStudentVC(vc: VerifiableCredential) {
        dataStore.edit { preferences ->
            preferences[KEY_STUDENT_VC] = gson.toJson(vc)
        }
    }
    
    /**
     * 운전면허증 전체 VC 저장
     */
    suspend fun saveDriverLicenseVC(vc: VerifiableCredential) {
        dataStore.edit { preferences ->
            preferences[KEY_DRIVER_LICENSE_VC] = gson.toJson(vc)
        }
    }
    
    /**
     * 학생증 전체 VC 조회
     */
    fun getStudentVC(): Flow<VerifiableCredential?> = dataStore.data.map { preferences ->
        preferences[KEY_STUDENT_VC]?.let { vcJson ->
            try {
                gson.fromJson(vcJson, VerifiableCredential::class.java)
            } catch (e: Exception) {
                android.util.Log.e("DIDLocalDataSource", "Failed to decode student VC", e)
                null
            }
        }
    }
    
    /**
     * 운전면허증 전체 VC 조회
     */
    fun getDriverLicenseVC(): Flow<VerifiableCredential?> = dataStore.data.map { preferences ->
        preferences[KEY_DRIVER_LICENSE_VC]?.let { vcJson ->
            try {
                gson.fromJson(vcJson, VerifiableCredential::class.java)
            } catch (e: Exception) {
                android.util.Log.e("DIDLocalDataSource", "Failed to decode driver license VC", e)
                null
            }
        }
    }
    
    /**
     * 모든 데이터 삭제
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}