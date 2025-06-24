package com.anam145.wallet.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.blockchainDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "blockchain_preferences"
)

/**
 * 블록체인 관련 설정을 관리하는 DataStore
 */
@Singleton
class BlockchainDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACTIVE_BLOCKCHAIN_ID = stringPreferencesKey("active_blockchain_id")
    }
    
    /**
     * 활성화된 블록체인 ID
     */
    val activeBlockchainId: Flow<String?> = context.blockchainDataStore.data
        .map { preferences ->
            preferences[ACTIVE_BLOCKCHAIN_ID]
        }
    
    /**
     * 활성 블록체인 ID 저장
     */
    suspend fun setActiveBlockchainId(blockchainId: String) {
        context.blockchainDataStore.edit { preferences ->
            preferences[ACTIVE_BLOCKCHAIN_ID] = blockchainId
        }
    }
    
    /**
     * 활성 블록체인 ID 삭제
     */
    suspend fun clearActiveBlockchainId() {
        context.blockchainDataStore.edit { preferences ->
            preferences.remove(ACTIVE_BLOCKCHAIN_ID)
        }
    }
}