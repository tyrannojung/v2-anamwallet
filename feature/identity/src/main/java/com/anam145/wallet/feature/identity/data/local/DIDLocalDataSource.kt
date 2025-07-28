package com.anam145.wallet.feature.identity.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anam145.wallet.feature.identity.domain.model.DIDCredentials
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
    }
    
    private val dataStore = context.didDataStore
    
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
     * 모든 데이터 삭제
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}