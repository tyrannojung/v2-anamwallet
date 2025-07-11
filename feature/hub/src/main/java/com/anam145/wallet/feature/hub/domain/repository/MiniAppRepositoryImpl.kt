package com.anam145.wallet.feature.hub.domain.repository

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.dao.MiniAppDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 앱 전체에서 인스턴스 하나만
class MiniAppRepositoryImpl @Inject constructor(
    private val miniAppDao: MiniAppDao
) : MiniAppRepository {

    override fun getMiniApps() : Flow<List<MiniApp>> {
        return miniAppDao.getMiniApps()
    }

    override suspend fun updateMiniApp(miniApp: MiniApp) {
        miniAppDao.updateMiniApp(miniApp)
    }

    override suspend fun deleteMiniApp(miniApp: MiniApp) {
        miniAppDao.deleteMiniApp(miniApp)
    }

    override suspend fun insertMiniApp(miniApp: MiniApp) {
        miniAppDao.insertMiniApp(miniApp)
    }

    override suspend fun insertMiniApps(miniApps: List<MiniApp>) {
        miniAppDao.insertMiniApps(miniApps)
    }

}