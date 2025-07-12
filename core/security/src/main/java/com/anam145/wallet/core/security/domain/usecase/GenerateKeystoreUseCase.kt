package com.anam145.wallet.core.security.domain.usecase

import com.anam145.wallet.core.security.data.util.CryptoUtils
import com.anam145.wallet.core.security.data.util.ScryptConstants
import com.anam145.wallet.core.security.model.CipherParams
import com.anam145.wallet.core.security.model.Crypto
import com.anam145.wallet.core.security.model.KeyStoreFile
import com.anam145.wallet.core.security.model.ScryptKdfParams
import com.google.gson.Gson
import com.lambdaworks.crypto.SCrypt
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * 키스토어 생성 UseCase
 * 비밀번호와 개인키를 사용하여 암호화된 키스토어를 생성합니다.
 */
class GenerateKeystoreUseCase @Inject constructor(
    private val gson: Gson
) {
    companion object {
        // 암호화 설정
        private const val CIPHER_ALGORITHM = "AES/CTR/NoPadding"
        private const val AES_KEY_SIZE = 16  // AES-128
    }
    
    /**
     * 키스토어 생성
     * 
     * @param password 사용자 비밀번호
     * @param address 지갑 주소
     * @param privateKey 개인키 (16진수 문자열)
     * @return 암호화된 키스토어 JSON 문자열
     */
    operator fun invoke(
        password: String,
        address: String,
        privateKey: String,
        useLightMode: Boolean = false
    ): Result<String> = runCatching {
        // 1. Salt 생성 (256비트)
        val salt = CryptoUtils.generateRandomBytes(32)
        
        // 2. SCrypt로 파생키 생성
        val n = if (useLightMode) ScryptConstants.N_LIGHT else ScryptConstants.N
        val derivedKey = SCrypt.scrypt(
            password.toByteArray(StandardCharsets.UTF_8),
            salt,
            n, ScryptConstants.R, ScryptConstants.P, ScryptConstants.DKLEN
        )
        
        // 3. 암호화 키 추출 (파생키의 앞 16바이트)
        val encryptKey = derivedKey.copyOfRange(0, AES_KEY_SIZE)
        
        // 4. IV 생성 (128비트)
        val iv = CryptoUtils.generateRandomBytes(16)
        
        // 5. 개인키를 바이트 배열로 변환
        val privateKeyBytes = CryptoUtils.hexStringToByteArray(privateKey)
        
        // 6. AES 암호화
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        val secretKeySpec = SecretKeySpec(encryptKey, "AES")
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
        val cipherText = cipher.doFinal(privateKeyBytes)
        
        // 7. MAC 생성 (무결성 검증용)
        val mac = CryptoUtils.generateMac(derivedKey, cipherText)
        
        // 8. KeyStoreFile 객체 구성
        val keyStoreFile = createKeyStoreFile(
            address = address,
            cipherText = cipherText,
            iv = iv,
            salt = salt,
            mac = mac,
            n = n
        )
        
        // 9. JSON으로 변환
        gson.toJson(keyStoreFile)
    }
    
    private fun createKeyStoreFile(
        address: String,
        cipherText: ByteArray,
        iv: ByteArray,
        salt: ByteArray,
        mac: ByteArray,
        n: Int = ScryptConstants.N
    ): KeyStoreFile {
        return KeyStoreFile(
            address = address,
            id = UUID.randomUUID().toString(),
            version = 3,
            crypto = Crypto(
                cipher = "aes-128-ctr",
                ciphertext = CryptoUtils.toHexStringNoPrefix(cipherText),
                cipherparams = CipherParams(
                    iv = CryptoUtils.toHexStringNoPrefix(iv)
                ),
                kdf = "scrypt",
                kdfparams = ScryptKdfParams(
                    dklen = ScryptConstants.DKLEN,
                    n = n,
                    r = ScryptConstants.R,
                    p = ScryptConstants.P,
                    salt = CryptoUtils.toHexStringNoPrefix(salt)
                ),
                mac = CryptoUtils.toHexStringNoPrefix(mac)
            )
        )
    }
}