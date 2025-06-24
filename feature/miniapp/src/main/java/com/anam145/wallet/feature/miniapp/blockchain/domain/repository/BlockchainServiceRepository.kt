package com.anam145.wallet.feature.miniapp.blockchain.domain.repository

import com.anam145.wallet.core.common.result.MiniAppResult
import kotlinx.coroutines.flow.Flow

/**
 * 블록체인 서비스와의 통신을 추상화하는 Repository Interface
 * 
 * BlockchainUIActivity에서 사용하는 서비스 연결을 담당합니다.
 */
interface BlockchainServiceRepository {
    
    /**
     * 서비스 연결 상태를 관찰합니다.
     * 
     * @return 서비스 연결 상태 Flow
     */
    fun observeServiceConnection(): Flow<Boolean>
    
    /**
     * 서비스에 연결합니다.
     * 
     * @return 연결 결과
     */
    suspend fun connectToService(): MiniAppResult<Unit>
    
    /**
     * 서비스 연결을 해제합니다.
     */
    suspend fun disconnectFromService()
    
    /**
     * 블록체인을 전환합니다.
     * 
     * @param blockchainId 전환할 블록체인 ID
     * @return 전환 결과
     */
    suspend fun switchBlockchain(blockchainId: String): MiniAppResult<Unit>
    
    /**
     * 현재 활성화된 블록체인 ID를 가져옵니다.
     * 
     * @return 활성 블록체인 ID
     */
    suspend fun getActiveBlockchainId(): MiniAppResult<String>
}