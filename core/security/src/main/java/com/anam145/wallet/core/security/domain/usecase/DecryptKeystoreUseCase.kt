package com.anam145.wallet.core.security.domain.usecase

import com.anam145.wallet.core.security.data.util.CryptoUtils
import com.anam145.wallet.core.security.model.Credentials
import com.anam145.wallet.core.security.model.KeyStoreFile
import com.anam145.wallet.core.security.model.ScryptKdfParams
import com.anam145.wallet.core.security.model.SecurityException
import com.google.gson.Gson
import com.lambdaworks.crypto.SCrypt
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * 키스토어 복호화 UseCase
 * 암호화된 키스토어에서 개인키를 추출합니다.
 */
class DecryptKeystoreUseCase @Inject constructor(
    private val gson: Gson
) {
    companion object {
        private const val CIPHER_ALGORITHM = "AES/CTR/NoPadding"
        private const val AES_KEY_SIZE = 16
    }
    
    /**
     * 키스토어 복호화
     * 
     * @param password 사용자 비밀번호
     * @param keystoreJson 키스토어 JSON 문자열
     * @return 복호화된 인증 정보 (주소, 개인키)
     */
    operator fun invoke(
        password: String,
        keystoreJson: String
    ): Result<Credentials> = runCatching {
        // 1. JSON 파싱
        val keyStoreFile = gson.fromJson(keystoreJson, KeyStoreFile::class.java)
        validateKeyStoreFile(keyStoreFile)
        
        val crypto = keyStoreFile.crypto
        
        // 2. 필요한 데이터 추출
        val mac = CryptoUtils.hexStringToByteArray(crypto.mac)
        val iv = CryptoUtils.hexStringToByteArray(crypto.cipherparams.iv)
        val cipherText = CryptoUtils.hexStringToByteArray(crypto.ciphertext)
        
        // 3. KDF 파라미터에 따라 파생키 재생성
        val derivedKey = when (val kdfParams = crypto.kdfparams) {
            is ScryptKdfParams -> {
                val salt = CryptoUtils.hexStringToByteArray(kdfParams.salt)
                SCrypt.scrypt(
                    password.toByteArray(StandardCharsets.UTF_8),
                    salt,
                    kdfParams.n,
                    kdfParams.r,
                    kdfParams.p,
                    kdfParams.dklen
                )
            }
            else -> throw SecurityException.CipherException("Unsupported KDF: ${crypto.kdf}")
        }
        
        // 4. MAC 검증 (비밀번호 확인)
        val derivedMac = CryptoUtils.generateMac(derivedKey, cipherText)
        if (!derivedMac.contentEquals(mac)) {
            throw SecurityException.CipherException("Invalid password provided")
        }
        
        // 5. 복호화 키 추출
        val encryptKey = derivedKey.copyOfRange(0, AES_KEY_SIZE)
        
        // 6. AES 복호화
        val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        val secretKeySpec = SecretKeySpec(encryptKey, "AES")
        
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
        val privateKeyBytes = cipher.doFinal(cipherText)
        
        // 7. 결과 반환
        Credentials(
            address = keyStoreFile.address,
            privateKey = CryptoUtils.toHexStringNoPrefix(privateKeyBytes)
        )
    }
    
    private fun validateKeyStoreFile(keyStoreFile: KeyStoreFile) {
        requireNotNull(keyStoreFile.crypto) { "Crypto object is missing" }
        requireNotNull(keyStoreFile.crypto.mac) { "MAC is missing" }
        requireNotNull(keyStoreFile.crypto.ciphertext) { "Ciphertext is missing" }
        requireNotNull(keyStoreFile.crypto.cipherparams) { "Cipher params are missing" }
        requireNotNull(keyStoreFile.crypto.cipherparams.iv) { "IV is missing" }
        requireNotNull(keyStoreFile.crypto.kdfparams) { "KDF params are missing" }
        
        require(keyStoreFile.crypto.cipher == "aes-128-ctr") {
            "Unsupported cipher: ${keyStoreFile.crypto.cipher}"
        }
    }
}