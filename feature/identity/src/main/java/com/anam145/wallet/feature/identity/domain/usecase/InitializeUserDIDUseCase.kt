package com.anam145.wallet.feature.identity.domain.usecase

import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import javax.inject.Inject

/**
 * 사용자 DID 신원 초기화 UseCase
 * 비밀번호 설정 시 자동으로 탈중앙화 신원을 생성
 */
class InitializeUserDIDUseCase @Inject constructor(
    private val didRepository: DIDRepository
) {
    companion object {
        private const val DEFAULT_USER_NAME = "Hong Gildong"
    }
    
    /**
     * DID 신원 초기화 실행
     * @return Result<Unit> 성공/실패
     */
    suspend operator fun invoke(): Result<Unit> {
        return didRepository.initializeDIDIdentity(DEFAULT_USER_NAME)
    }
}