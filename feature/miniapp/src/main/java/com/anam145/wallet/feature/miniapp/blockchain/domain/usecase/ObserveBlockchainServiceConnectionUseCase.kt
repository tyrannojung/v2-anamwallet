package com.anam145.wallet.feature.miniapp.blockchain.domain.usecase

import com.anam145.wallet.feature.miniapp.blockchain.domain.repository.BlockchainServiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 블록체인 서비스 연결 상태를 관찰하는 UseCase
 * 
 * BlockchainViewModel에서 서비스 연결 상태를 지속적으로 모니터링하기 위해 사용됩니다.
 * 서비스가 메모리 부족 등의 이유로 종료될 수 있으므로, 연결 상태를 실시간으로 추적합니다.
 */
class ObserveBlockchainServiceConnectionUseCase @Inject constructor(
    private val repository: BlockchainServiceRepository
) {
    /**
     * 서비스 연결 상태를 관찰합니다.
     * 
     * @return 연결 상태를 나타내는 Boolean Flow
     *         - true: 서비스 연결됨
     *         - false: 서비스 연결 끊김
     */
    operator fun invoke(): Flow<Boolean> = repository.observeServiceConnection()
}