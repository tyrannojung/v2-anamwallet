package com.anam145.wallet.core.security.data.util

import org.bouncycastle.jcajce.provider.digest.Keccak
import java.security.SecureRandom

/**
 * 암호화 관련 유틸리티 함수들
 */
object CryptoUtils {
    private const val HEX_PREFIX = "0x"
    private val HEX_CHAR_MAP = "0123456789abcdef".toCharArray()
    
    /**
     * 랜덤 바이트 배열 생성
     */
    @JvmStatic
    fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
    
    /**
     * 바이트 배열을 16진수 문자열로 변환 (0x 접두사 없음)
     */
    @JvmStatic
    fun toHexStringNoPrefix(input: ByteArray): String {
        val stringBuilder = StringBuilder(input.size * 2)
        for (b in input) {
            stringBuilder.append(HEX_CHAR_MAP[(b.toInt() shr 4) and 0x0f])
            stringBuilder.append(HEX_CHAR_MAP[b.toInt() and 0x0f])
        }
        return stringBuilder.toString()
    }
    
    /**
     * 16진수 문자열을 바이트 배열로 변환
     * @throws IllegalArgumentException 입력이 유효하지 않은 16진수 문자열인 경우
     */
    @JvmStatic
    fun hexStringToByteArray(input: String): ByteArray {
        val cleanInput = if (input.startsWith(HEX_PREFIX)) {
            input.substring(2)
        } else {
            input
        }
        
        // 홀수 길이 검증
        require(cleanInput.length % 2 == 0) {
            "Hex string must have even length: ${cleanInput.length}"
        }
        
        // 16진수 문자 검증
        require(cleanInput.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            "Invalid hex string: contains non-hexadecimal characters"
        }
        
        val len = cleanInput.length
        val data = ByteArray(len / 2)
        
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(cleanInput[i], 16) shl 4) +
                    Character.digit(cleanInput[i + 1], 16)).toByte()
            i += 2
        }
        
        return data
    }
    
    /**
     * MAC (Message Authentication Code) 생성
     * Keccak-256 해시 사용
     */
    @JvmStatic
    fun generateMac(derivedKey: ByteArray, cipherText: ByteArray): ByteArray {
        val result = ByteArray(16 + cipherText.size)
        
        // derivedKey의 16번째 바이트부터 16바이트를 복사
        System.arraycopy(derivedKey, 16, result, 0, 16)
        // cipherText 전체를 복사
        System.arraycopy(cipherText, 0, result, 16, cipherText.size)
        
        val keccak = Keccak.Digest256()
        return keccak.digest(result)
    }
}