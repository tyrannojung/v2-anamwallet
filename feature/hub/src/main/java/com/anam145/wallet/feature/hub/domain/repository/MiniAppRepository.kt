package com.anam145.wallet.feature.hub.domain.repository

import com.anam145.wallet.core.common.model.MiniApp
import kotlinx.coroutines.flow.Flow

interface MiniAppRepository {

    fun getMiniApps(): Flow<List<MiniApp>>

    suspend fun updateMiniApp(miniApp: MiniApp)

    suspend fun deleteMiniApp(miniApp: MiniApp)

    suspend fun insertMiniApp(miniApp: MiniApp)

    suspend fun insertMiniApps(miniApps: List<MiniApp>)
}