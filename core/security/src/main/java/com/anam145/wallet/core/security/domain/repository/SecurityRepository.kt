package com.anam145.wallet.core.security.domain.repository

import com.anam145.wallet.core.security.model.ScryptParams
import kotlinx.coroutines.flow.Flow

/**
 * 보안 관련 데이터 저장소 인터페이스
 * 
 * 앱 비밀번호와 관련된 데이터의 저장 및 조회를 담당합니다.
 * Clean Architecture의 Repository 패턴을 따라 domain 레이어에 인터페이스를 정의하고
 * data 레이어에서 실제 구현을 제공합니다.
 */
interface SecurityRepository {
    
    /**
     * 비밀번호 해시와 관련 정보를 저장합니다.
     * 
     * @param passwordHash SCrypt로 해싱된 비밀번호
     * @param salt 해싱에 사용된 salt
     * @param scryptParams SCrypt 파라미터 정보
     */
    suspend fun savePasswordHash(
        passwordHash: ByteArray,
        salt: ByteArray,
        scryptParams: ScryptParams
    ): Result<Unit>
    
    /**
     * 저장된 비밀번호 해시를 조회합니다.
     * 
     * @return 비밀번호 해시, 없으면 null
     */
    suspend fun getPasswordHash(): ByteArray?
    
    /**
     * 저장된 salt를 조회합니다.
     * 
     * @return salt 값, 없으면 null
     */
    suspend fun getSalt(): ByteArray?
    
    /**
     * 저장된 SCrypt 파라미터를 조회합니다.
     * 
     * @return SCrypt 파라미터, 없으면 null
     */
    suspend fun getScryptParams(): ScryptParams?
    
    /**
     * 비밀번호가 설정되어 있는지 확인합니다.
     * 
     * @return 비밀번호 존재 여부를 Flow로 반환
     */
    fun hasPassword(): Flow<Boolean>
    
    /**
     * 모든 보안 데이터를 삭제합니다.
     * 주로 앱 초기화나 로그아웃 시 사용됩니다.
     */
    suspend fun clearAll(): Result<Unit>
}