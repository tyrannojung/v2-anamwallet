package com.anam145.wallet.feature.miniapp.webapp.domain.repository

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.model.TransactionRequest
import com.anam145.wallet.feature.miniapp.common.domain.model.TransactionResponse
import kotlinx.coroutines.flow.Flow

/**
 * WebApp 서비스와의 통신을 추상화하는 Repository Interface
 * 
 * AIDL 서비스 연결 및 결제 처리를 담당합니다.
 */
interface WebAppServiceRepository {
    
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
     * 현재 활성화된 블록체인 ID를 가져옵니다.
     * 
     * @return 활성 블록체인 ID
     */
    suspend fun getActiveBlockchainId(): MiniAppResult<String>
    
    /**
     * 트랜잭션을 요청합니다.
     * 
     * @param request 트랜잭션 요청 정보
     * @return 트랜잭션 응답
     */
    suspend fun requestTransaction(request: TransactionRequest): MiniAppResult<TransactionResponse>
}