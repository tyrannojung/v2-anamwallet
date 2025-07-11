package com.anam145.wallet.core.security.domain.usecase

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.lambdaworks.crypto.SCrypt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Named

/**
 * 앱 비밀번호 검증 UseCase
 * 입력된 비밀번호가 저장된 비밀번호와 일치하는지 확인합니다.
 */
class VerifyAppPasswordUseCase @Inject constructor(
    @Named("security") private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {
    companion object {
        private val PASSWORD_HASH_KEY = stringPreferencesKey("app_password_hash")
        private val PASSWORD_SALT_KEY = stringPreferencesKey("app_password_salt")
        private val SCRYPT_PARAMS_KEY = stringPreferencesKey("scrypt_params")
        private const val DKLEN = 32
    }
    
    /**
     * 앱 비밀번호 검증
     * 
     * @param inputPassword 입력된 비밀번호
     * @return 비밀번호가 맞으면 true
     */
    suspend operator fun invoke(inputPassword: String): Result<Boolean> = runCatching {
        val preferences = dataStore.data.first()
        
        val hashString = preferences[PASSWORD_HASH_KEY] ?: return@runCatching false
        val saltString = preferences[PASSWORD_SALT_KEY] ?: return@runCatching false
        val paramsString = preferences[SCRYPT_PARAMS_KEY] ?: return@runCatching false
        
        val params = gson.fromJson(paramsString, ScryptParams::class.java)
        
        // Salt 디코딩
        val salt = Base64.decode(saltString, Base64.NO_WRAP)
        val storedHash = Base64.decode(hashString, Base64.NO_WRAP)
        
        // 입력된 비밀번호로 해시 재생성
        val inputHash = SCrypt.scrypt(
            inputPassword.toByteArray(StandardCharsets.UTF_8),
            salt,
            params.n,
            params.r,
            params.p,
            DKLEN
        )
        
        // 비교
        storedHash.contentEquals(inputHash)
    }
    
    /**
     * 저장된 비밀번호 존재 여부 확인
     */
    suspend fun hasPassword(): Boolean {
        return dataStore.data
            .map { preferences ->
                preferences[PASSWORD_HASH_KEY] != null
            }
            .first()
    }
    
    private data class ScryptParams(
        val n: Int,
        val r: Int,
        val p: Int
    )
}