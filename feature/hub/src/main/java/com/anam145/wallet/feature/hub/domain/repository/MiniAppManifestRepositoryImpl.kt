package com.anam145.wallet.feature.hub.domain.repository

import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.hub.domain.dao.MiniAppManifestDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // 앱 전체에서 인스턴스 하나만
class MiniAppManifestRepositoryImpl @Inject constructor(
    private val miniAppFestDao: MiniAppManifestDao
) {
    fun getMiniAppManifest() : Flow<List<MiniAppManifest>>{
        return miniAppFestDao.getMiniAppManifest()
    }

    suspend fun updateMiniAppManifest(miniAppManifest: MiniAppManifest) {
        miniAppFestDao.updateMiniAppManifest(miniAppManifest)
    }

    suspend fun deleteMiniAppManifest(miniAppManifest: MiniAppManifest) {
        miniAppFestDao.deleteMiniAppManifest(miniAppManifest)
    }

}