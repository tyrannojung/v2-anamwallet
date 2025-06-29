// MiniAppManifestRepository.kt (인터페이스)
package com.anam145.wallet.feature.hub.domain.repository

import com.anam145.wallet.core.common.model.MiniAppManifest
import kotlinx.coroutines.flow.Flow

interface MiniAppManifestRepository {
    fun getMiniAppManifest(): Flow<List<MiniAppManifest>>
    suspend fun updateMiniAppManifest(miniAppManifest: MiniAppManifest)
    suspend fun deleteMiniAppManifest(miniAppManifest: MiniAppManifest)
}