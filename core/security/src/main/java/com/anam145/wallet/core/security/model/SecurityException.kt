package com.anam145.wallet.core.security.model

/**
 * 보안 관련 예외 클래스
 */
sealed class SecurityException(message: String) : Exception(message) {
    
    /**
     * 암호 관련 예외
     */
    class CipherException(message: String) : SecurityException(message)
    
    /**
     * 키스토어 관련 예외
     */
    class KeystoreException(message: String) : SecurityException(message)
    
    /**
     * 인증 실패 예외
     */
    class AuthenticationException(message: String) : SecurityException(message)
}