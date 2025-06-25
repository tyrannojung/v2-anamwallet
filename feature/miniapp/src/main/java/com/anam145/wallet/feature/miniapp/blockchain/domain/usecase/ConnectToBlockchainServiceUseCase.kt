package com.anam145.wallet.feature.miniapp.blockchain.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.blockchain.domain.repository.BlockchainServiceRepository
import javax.inject.Inject

/**
 * 블록체인 서비스에 연결하는 UseCase
 * 
 * BlockchainActivity가 시작될 때 블록체인 서비스에 바인딩하기 위해 사용됩니다.
 * 같은 프로세스 내에서도 Activity와 Service 간 AIDL 통신을 위해 바인딩이 필요합니다.
 */
class ConnectToBlockchainServiceUseCase @Inject constructor(
    private val repository: BlockchainServiceRepository
) {
    /**
     * 블록체인 서비스에 연결합니다.
     * 
     * @return 연결 성공/실패 결과
     */
    suspend operator fun invoke(): MiniAppResult<Unit> = repository.connectToService()
}