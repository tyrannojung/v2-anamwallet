package com.anam145.wallet.core.security.keystore

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.StringWriter
import java.security.*
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.util.io.pem.PemObject

/**
 * DID 키 관리자
 * EncryptedSharedPreferences를 사용하여 키를 안전하게 저장
 */
@Singleton
class DIDKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "did_keystore"
        private const val KEY_PUBLIC_SUFFIX = "_public"
        private const val KEY_PRIVATE_SUFFIX = "_private"
        
        init {
            // BouncyCastle Provider 등록
            Security.removeProvider("BC")
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * EC 키쌍 생성 및 저장 (secp256r1)
     * @return Pair<PublicKeyPem, PrivateKeyPem>
     */
    fun generateAndStoreKeyPair(alias: String): Pair<String, String> {
        try {
            // EC 키쌍 생성 (secp256r1 = P-256)
            val keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC")
            val ecSpec = ECGenParameterSpec("secp256r1")
            keyPairGenerator.initialize(ecSpec, SecureRandom())
            val keyPair = keyPairGenerator.generateKeyPair()
            
            // PEM 형식으로 변환
            val publicKeyPem = convertToPem(keyPair.public, "PUBLIC KEY")
            val privateKeyPem = convertToPem(keyPair.private, "EC PRIVATE KEY")
            
            // 암호화하여 저장
            encryptedPrefs.edit()
                .putString(alias + KEY_PUBLIC_SUFFIX, publicKeyPem)
                .putString(alias + KEY_PRIVATE_SUFFIX, privateKeyPem)
                .apply()
                
            return Pair(publicKeyPem, privateKeyPem)
            
        } catch (e: Exception) {
            throw RuntimeException("키 생성 실패: ${e.message}", e)
        }
    }
    
    /**
     * 저장된 키 존재 여부 확인
     */
    fun hasKey(alias: String): Boolean {
        return encryptedPrefs.contains(alias + KEY_PUBLIC_SUFFIX) &&
               encryptedPrefs.contains(alias + KEY_PRIVATE_SUFFIX)
    }
    
    /**
     * 공개키 조회
     */
    fun getPublicKey(alias: String): String? {
        return encryptedPrefs.getString(alias + KEY_PUBLIC_SUFFIX, null)
    }
    
    /**
     * 개인키 조회 (내부 사용)
     */
    private fun getPrivateKey(alias: String): String? {
        return encryptedPrefs.getString(alias + KEY_PRIVATE_SUFFIX, null)
    }
    
    /**
     * PEM을 Base64로 변환 (헤더/푸터 제거)
     */
    fun pemToBase64(pem: String): String {
        val cleanPem = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----BEGIN EC PRIVATE KEY-----", "")
            .replace("-----END EC PRIVATE KEY-----", "")
            .replace("\n", "")
            .trim()
        return cleanPem
    }
    
    /**
     * 키를 PEM 형식으로 변환
     */
    private fun convertToPem(key: Key, type: String): String {
        val stringWriter = StringWriter()
        JcaPEMWriter(stringWriter).use { pemWriter ->
            pemWriter.writeObject(PemObject(type, key.encoded))
        }
        return stringWriter.toString()
    }
    
    /**
     * 모든 키 삭제
     */
    fun clearAllKeys() {
        encryptedPrefs.edit().clear().apply()
    }
}