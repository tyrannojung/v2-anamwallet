package com.anam145.wallet.feature.hub.domain.repository

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.dao.MiniAppDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 앱 전체에서 인스턴스 하나만
class MiniAppRepositoryImpl @Inject constructor(
    private val miniAppDao: MiniAppDao
) {

    fun getMiniApps() : Flow<List<MiniApp>> {
        return miniAppDao.getMiniApps()
    }

    suspend fun updateMiniApp(miniApp: MiniApp) {
        miniAppDao.updateMiniApp(miniApp)
    }

    suspend fun deleteMiniApp(miniApp: MiniApp) {
        miniAppDao.deleteMiniApp(miniApp)
    }

}