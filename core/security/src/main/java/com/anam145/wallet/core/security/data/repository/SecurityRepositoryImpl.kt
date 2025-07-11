package com.anam145.wallet.core.security.data.repository

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.anam145.wallet.core.security.domain.repository.SecurityRepository
import com.anam145.wallet.core.security.model.ScryptParams
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

/**
 * SecurityRepository 구현체
 * 
 * DataStore를 사용하여 앱 비밀번호 관련 데이터를 안전하게 저장하고 관리합니다.
 */
class SecurityRepositoryImpl @Inject constructor(
    @Named("security") private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) : SecurityRepository {
    
    companion object {
        private val PASSWORD_HASH_KEY = stringPreferencesKey("app_password_hash")
        private val PASSWORD_SALT_KEY = stringPreferencesKey("app_password_salt")
        private val SCRYPT_PARAMS_KEY = stringPreferencesKey("scrypt_params")
    }
    
    override suspend fun savePasswordHash(
        passwordHash: ByteArray,
        salt: ByteArray,
        scryptParams: ScryptParams
    ): Result<Unit> = runCatching {
        val encodedHash = Base64.encodeToString(passwordHash, Base64.NO_WRAP)
        val encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP)
        val paramsJson = gson.toJson(scryptParams)
        
        dataStore.edit { preferences ->
            preferences[PASSWORD_HASH_KEY] = encodedHash
            preferences[PASSWORD_SALT_KEY] = encodedSalt
            preferences[SCRYPT_PARAMS_KEY] = paramsJson
        }
    }
    
    override suspend fun getPasswordHash(): ByteArray? {
        val preferences = dataStore.data.first()
        val hashString = preferences[PASSWORD_HASH_KEY] ?: return null
        return Base64.decode(hashString, Base64.NO_WRAP)
    }
    
    override suspend fun getSalt(): ByteArray? {
        val preferences = dataStore.data.first()
        val saltString = preferences[PASSWORD_SALT_KEY] ?: return null
        return Base64.decode(saltString, Base64.NO_WRAP)
    }
    
    override suspend fun getScryptParams(): ScryptParams? {
        val preferences = dataStore.data.first()
        val paramsString = preferences[SCRYPT_PARAMS_KEY] ?: return null
        return gson.fromJson(paramsString, ScryptParams::class.java)
    }
    
    override fun hasPassword(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PASSWORD_HASH_KEY] != null
        }
    }
    
    override suspend fun clearAll(): Result<Unit> = runCatching {
        dataStore.edit { preferences ->
            preferences.remove(PASSWORD_HASH_KEY)
            preferences.remove(PASSWORD_SALT_KEY)
            preferences.remove(SCRYPT_PARAMS_KEY)
        }
    }
}