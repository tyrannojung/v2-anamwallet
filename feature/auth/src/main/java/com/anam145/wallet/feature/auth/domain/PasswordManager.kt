package com.anam145.wallet.feature.auth.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 비밀번호 관리 싱글톤
 * 메모리에 비밀번호를 캐싱합니다.
 * 앱이 재시작되면 자동으로 초기화됩니다.
 */
@Singleton
class PasswordManager @Inject constructor() {
    
    private var cachedPassword: String? = null
    
    /**
     * 비밀번호 설정 및 캐싱
     */
    fun setPassword(password: String) {
        cachedPassword = password
    }
    
    /**
     * 캐싱된 비밀번호 가져오기
     */
    fun getPassword(): String? {
        return cachedPassword
    }
    
    /**
     * 인증 상태 확인
     */
    fun isAuthenticated(): Boolean {
        return cachedPassword != null
    }
    
    /**
     * 비밀번호 지우기
     */
    fun clearPassword() {
        cachedPassword = null
    }
}