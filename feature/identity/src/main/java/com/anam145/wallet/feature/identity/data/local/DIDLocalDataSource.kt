package com.anam145.wallet.feature.identity.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anam145.wallet.feature.identity.domain.model.DIDCredentials
import com.anam145.wallet.feature.identity.domain.model.IssuedCredential
import com.google.gson.Gson
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
        
        // VC 관련 키
        private val KEY_STUDENT_VC_ID = stringPreferencesKey("studentVcId")
        private val KEY_DRIVER_LICENSE_VC_ID = stringPreferencesKey("driverLicenseVcId")
        private val KEY_STUDENT_CARD_INFO = stringPreferencesKey("studentCardInfo")
        private val KEY_DRIVER_LICENSE_INFO = stringPreferencesKey("driverLicenseInfo")
    }
    
    private val dataStore = context.didDataStore
    private val gson = Gson()
    
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
     * 학생증 VC 정보 저장
     */
    suspend fun saveStudentCardVC(vcId: String, info: IssuedCredential) {
        dataStore.edit { preferences ->
            preferences[KEY_STUDENT_VC_ID] = vcId
            preferences[KEY_STUDENT_CARD_INFO] = gson.toJson(info)
        }
    }
    
    /**
     * 운전면허증 VC 정보 저장
     */
    suspend fun saveDriverLicenseVC(vcId: String, info: IssuedCredential) {
        dataStore.edit { preferences ->
            preferences[KEY_DRIVER_LICENSE_VC_ID] = vcId
            preferences[KEY_DRIVER_LICENSE_INFO] = gson.toJson(info)
        }
    }
    
    /**
     * 발급된 VC 목록 조회
     */
    fun getIssuedCredentials(): Flow<List<IssuedCredential>> = dataStore.data.map { preferences ->
        val credentials = mutableListOf<IssuedCredential>()
        
        preferences[KEY_STUDENT_CARD_INFO]?.let { infoJson ->
            try {
                credentials.add(
                    gson.fromJson(infoJson, IssuedCredential::class.java)
                )
            } catch (e: Exception) {
                android.util.Log.e("DIDLocalDataSource", "Failed to decode student card info", e)
            }
        }
        
        preferences[KEY_DRIVER_LICENSE_INFO]?.let { infoJson ->
            try {
                credentials.add(
                    gson.fromJson(infoJson, IssuedCredential::class.java)
                )
            } catch (e: Exception) {
                android.util.Log.e("DIDLocalDataSource", "Failed to decode driver license info", e)
            }
        }
        
        credentials
    }
    
    /**
     * 학생증 발급 여부
     */
    fun isStudentCardIssued(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_STUDENT_VC_ID] != null
    }
    
    /**
     * 운전면허증 발급 여부
     */
    fun isDriverLicenseIssued(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DRIVER_LICENSE_VC_ID] != null
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