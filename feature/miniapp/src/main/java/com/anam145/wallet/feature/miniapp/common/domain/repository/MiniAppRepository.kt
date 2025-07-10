package com.anam145.wallet.feature.miniapp.common.domain.repository

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.core.common.result.MiniAppResult

interface MiniAppRepository {
    suspend fun initializeMiniApps(): MiniAppResult<Unit>
    suspend fun getInstalledMiniApps(): MiniAppResult<List<MiniApp>>
    suspend fun loadMiniAppManifest(appId: String): MiniAppResult<MiniAppManifest>
    suspend fun isMiniAppsInitialized(): Boolean
    suspend fun clearCache()
}