package com.anam145.wallet.core.security.domain.usecase

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.anam145.wallet.core.security.data.util.ScryptConstants
import com.google.gson.Gson
import com.lambdaworks.crypto.SCrypt
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Named

/**
 * 앱 비밀번호 저장 UseCase
 * 사용자의 앱 접근 비밀번호를 안전하게 해싱하여 저장합니다.
 */
class SaveAppPasswordUseCase @Inject constructor(
    @Named("security") private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {
    companion object {
        private val PASSWORD_HASH_KEY = stringPreferencesKey("app_password_hash")
        private val PASSWORD_SALT_KEY = stringPreferencesKey("app_password_salt")
        private val SCRYPT_PARAMS_KEY = stringPreferencesKey("scrypt_params")
    }
    
    /**
     * 앱 비밀번호 저장
     * 
     * @param password 사용자 비밀번호
     * @return 저장 성공 여부
     */
    suspend operator fun invoke(password: String): Result<Unit> = runCatching {
        // Salt 생성
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        
        // SCrypt로 해시 생성
        val hash = SCrypt.scrypt(
            password.toByteArray(StandardCharsets.UTF_8),
            salt,
            ScryptConstants.N, 
            ScryptConstants.R, 
            ScryptConstants.P, 
            ScryptConstants.DKLEN
        )
        
        // DataStore에 저장
        val encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP)
        val encodedHash = Base64.encodeToString(hash, Base64.NO_WRAP)
        val scryptParams = ScryptParams(ScryptConstants.N, ScryptConstants.R, ScryptConstants.P)
        
        dataStore.edit { preferences ->
            preferences[PASSWORD_HASH_KEY] = encodedHash
            preferences[PASSWORD_SALT_KEY] = encodedSalt
            preferences[SCRYPT_PARAMS_KEY] = gson.toJson(scryptParams)
        }
    }
    
    private data class ScryptParams(
        val n: Int,
        val r: Int,
        val p: Int
    )
}