package com.anam145.wallet.feature.identity.domain.repository

import com.anam145.wallet.feature.identity.domain.model.DIDDocument
import com.anam145.wallet.feature.identity.domain.model.DIDCredentials

/**
 * DID 관련 비즈니스 로직을 위한 Repository 인터페이스
 */
interface DIDRepository {
    
    /**
     * DID 신원 초기화 - 사용자의 탈중앙화 신원을 생성하고 블록체인에 등록
     * @param userName 사용자 이름 (메타데이터)
     * @return 성공/실패 결과
     */
    suspend fun initializeDIDIdentity(userName: String): Result<Unit>
    
    /**
     * DID 자격증명 조회 - 저장된 사용자의 DID 정보와 공개키를 반환
     * @return DID 자격증명 정보 (userId, userDid, publicKey)
     */
    suspend fun getDIDCredentials(): DIDCredentials?
    
    /**
     * DID Document 조회
     * @param did DID 식별자
     * @return DID Document
     */
    suspend fun getDIDDocument(did: String): Result<DIDDocument>
    
    /**
     * DID 초기화 상태 확인
     * @return DID가 초기화되었는지 여부
     */
    suspend fun isDIDInitialized(): Boolean
}