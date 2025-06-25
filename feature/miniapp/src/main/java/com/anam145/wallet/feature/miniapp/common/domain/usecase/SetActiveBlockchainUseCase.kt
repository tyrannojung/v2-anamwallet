package com.anam145.wallet.feature.miniapp.common.domain.usecase

import com.anam145.wallet.feature.miniapp.common.domain.repository.BlockchainPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 활성 블록체인 ID를 저장하는 UseCase
 * 
 * Clean Architecture 원칙에 따라 Repository를 통해 데이터에 접근합니다.
 * ViewModel → UseCase → Repository → DataStore 순서로 의존성이 흐릅니다.
 */
@Singleton
class SetActiveBlockchainUseCase @Inject constructor(
    private val repository: BlockchainPreferencesRepository
) {
    /**
     * 활성 블록체인 ID를 저장합니다.
     * 
     * @param blockchainId 저장할 블록체인 ID
     */
    suspend operator fun invoke(blockchainId: String) {
        repository.setActiveBlockchainId(blockchainId)
    }
}