package com.anam145.wallet.feature.miniapp.common.data.repository

import com.anam145.wallet.core.data.datastore.BlockchainDataStore
import com.anam145.wallet.feature.miniapp.common.domain.repository.BlockchainPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BlockchainPreferencesRepository의 구현체
 * 
 * 실제 데이터 저장은 BlockchainDataStore에 위임합니다.
 * Clean Architecture에서 Repository 구현체는 data 레이어에 위치합니다.
 */
@Singleton
class BlockchainPreferencesRepositoryImpl @Inject constructor(
    private val blockchainDataStore: BlockchainDataStore
) : BlockchainPreferencesRepository {
    
    override val activeBlockchainId: Flow<String?> = blockchainDataStore.activeBlockchainId
    
    override suspend fun setActiveBlockchainId(blockchainId: String) {
        blockchainDataStore.setActiveBlockchainId(blockchainId)
    }
}