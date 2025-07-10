package com.anam145.wallet.feature.miniapp.common.data.repository

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.core.data.datastore.MiniAppDataStore
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppScanner
import com.anam145.wallet.feature.miniapp.common.domain.repository.MiniAppRepository
import com.anam145.wallet.core.common.result.MiniAppResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MiniAppRepositoryImpl @Inject constructor(
    private val fileManager: MiniAppFileManager,
    private val scanner: MiniAppScanner,
    private val dataStore: MiniAppDataStore
) : MiniAppRepository {
    
    override suspend fun initializeMiniApps(): MiniAppResult<Unit> {
        return try {
            /**
             * Flow는 값이 변경될 때마다 "방출"하므로,
             * first() 호출 시, 호출 시점 현재 값 바로 확인하는 것.
             * 이와 반대로 collect는 변경사항 계속 관찰하는 역할.(settings에서 사용)
             * */
            val isInitialized = dataStore.isMiniAppsInitialized.first()
            
            if (!isInitialized) {
                when (val result = fileManager.installAllFromAssets()) {
                    // 리스트 반환 x, 설치만 진행
                    is MiniAppResult.Success -> {
                        // 캐시 클리어하여 최신 데이터 읽도록 보장
                        clearCache()
                        dataStore.setMiniAppsInitialized(true)
                        result
                    }
                    is MiniAppResult.Error -> result
                }
            } else {
                MiniAppResult.Success(Unit)
            }
        } catch (e: Exception) {
            MiniAppResult.Error.Unknown(e)
        }
    }
    
    override suspend fun getInstalledMiniApps(): MiniAppResult<List<MiniApp>> {
        // 직접 scanner의 MiniAppResult를 반환
        return scanner.scanInstalledApps()
    }
    
    override suspend fun loadMiniAppManifest(appId: String): MiniAppResult<MiniAppManifest> {
        // 직접 fileManager의 MiniAppResult를 반환
        return fileManager.loadManifest(appId)
    }
    
    override suspend fun isMiniAppsInitialized(): Boolean {
        return dataStore.isMiniAppsInitialized.first()
    }
    
    override suspend fun clearCache() {
        scanner.clearCache()
    }
}