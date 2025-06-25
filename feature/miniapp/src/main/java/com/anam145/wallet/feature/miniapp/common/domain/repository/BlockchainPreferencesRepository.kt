package com.anam145.wallet.feature.miniapp.common.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 블록체인 관련 설정을 관리하는 Repository 인터페이스
 * 
 * Clean Architecture 원칙에 따라 UseCase는 이 인터페이스를 통해서만
 * 데이터 레이어에 접근합니다.
 */
interface BlockchainPreferencesRepository {
    
    /**
     * 현재 활성화된 블록체인 ID를 관찰합니다.
     * 
     * @return 활성 블록체인 ID의 Flow (null일 수 있음)
     */
    val activeBlockchainId: Flow<String?>
    
    /**
     * 활성 블록체인 ID를 설정합니다.
     * 
     * @param blockchainId 설정할 블록체인 ID
     */
    suspend fun setActiveBlockchainId(blockchainId: String)
    
}