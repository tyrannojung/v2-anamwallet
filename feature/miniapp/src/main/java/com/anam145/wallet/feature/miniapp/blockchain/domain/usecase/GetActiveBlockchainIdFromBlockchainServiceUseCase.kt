package com.anam145.wallet.feature.miniapp.blockchain.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.usecase.GetActiveBlockchainIdUseCase
import com.anam145.wallet.feature.miniapp.blockchain.domain.repository.BlockchainServiceRepository
import javax.inject.Inject

/**
 * BlockchainService로부터 현재 활성화된 블록체인 ID를 가져오는 UseCase
 * 
 * BlockchainViewModel에서 현재 블록체인이 활성화되어 있는지 확인하기 위해 사용합니다.
 */
class GetActiveBlockchainIdFromBlockchainServiceUseCase @Inject constructor(
    private val repository: BlockchainServiceRepository
) : GetActiveBlockchainIdUseCase {
    
    override suspend operator fun invoke(): MiniAppResult<String> {
        return repository.getActiveBlockchainId()
    }
}