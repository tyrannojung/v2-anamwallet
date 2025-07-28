package com.anam145.wallet.feature.identity.domain.repository

import com.anam145.wallet.feature.identity.domain.model.*
import kotlinx.coroutines.flow.Flow

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
    
    /**
     * 학생증 발급
     * @return 발급된 VerifiableCredential
     */
    suspend fun issueStudentCard(): Result<VerifiableCredential>
    
    /**
     * 운전면허증 발급
     * @return 발급된 VerifiableCredential
     */
    suspend fun issueDriverLicense(): Result<VerifiableCredential>
    
    /**
     * 발급된 자격증명 목록 조회
     * @return 발급된 자격증명 목록 Flow
     */
    fun getIssuedCredentials(): Flow<List<IssuedCredential>>
    
    /**
     * 학생증 발급 여부
     * @return 학생증 발급 여부 Flow
     */
    fun isStudentCardIssued(): Flow<Boolean>
    
    /**
     * 운전면허증 발급 여부
     * @return 운전면허증 발급 여부 Flow
     */
    fun isDriverLicenseIssued(): Flow<Boolean>
}