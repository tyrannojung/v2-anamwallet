package com.anam145.wallet.feature.miniapp.domain.usecase

import com.anam145.wallet.feature.miniapp.domain.repository.BlockchainPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 활성 블록체인 ID를 가져오는 UseCase
 * 
 * Clean Architecture 원칙에 따라 Repository를 통해 데이터에 접근합니다.
 * ViewModel → UseCase → Repository → DataStore 순서로 의존성이 흐릅니다.
 */
@Singleton
class GetActiveBlockchainUseCase @Inject constructor(
    private val repository: BlockchainPreferencesRepository
) {
    /**
     * 활성 블록체인 ID를 Flow로 반환합니다.
     * 
     * @return 활성 블록체인 ID의 Flow (null일 수 있음)
     */
    operator fun invoke(): Flow<String?> = repository.activeBlockchainId
}