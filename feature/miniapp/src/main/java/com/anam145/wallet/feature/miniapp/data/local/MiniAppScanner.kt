package com.anam145.wallet.feature.miniapp.data.local

import android.util.Log
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.feature.miniapp.data.MiniAppConstants
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.core.common.result.onSuccess
import com.anam145.wallet.core.common.result.onError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiniAppScanner @Inject constructor(
    private val fileManager: MiniAppFileManager
) {
    companion object {
        private const val TAG = "MiniAppScanner"
    }
    
    private data class CachedData(
        val apps: List<MiniApp>,
        val timestamp: Long
    )
    
    private var cache: CachedData? = null
    
    suspend fun scanInstalledApps(forceRefresh: Boolean = false): MiniAppResult<List<MiniApp>> = withContext(Dispatchers.IO) {
        try {
            // 캐시 확인
            val cached = cache
            if (!forceRefresh && cached != null && 
                System.currentTimeMillis() - cached.timestamp < MiniAppConstants.CACHE_DURATION_MS) {
                Log.d(TAG, "Returning cached apps: ${cached.apps.size}")
                return@withContext MiniAppResult.Success(cached.apps)
            }
            
            val installedApps = mutableListOf<MiniApp>()
            val appIds = fileManager.getInstalledApps()
            
            if (appIds.isEmpty()) {
                return@withContext MiniAppResult.Error.NoAppsInstalled
            }
            
            Log.d(TAG, "Scanning ${appIds.size} installed apps")
            
            appIds.forEach { appId ->
                try {
                    fileManager.loadManifest(appId)
                        .onSuccess { manifest ->
                            val miniApp = MiniApp(
                                appId = manifest.appId,
                                name = manifest.name,
                                type = when (manifest.type) {
                                    MiniAppConstants.TYPE_BLOCKCHAIN -> MiniAppType.BLOCKCHAIN
                                    else -> MiniAppType.APP
                                },
                                iconPath = fileManager.getMiniAppBasePath(appId) + MiniAppConstants.ICON_PATH,
                                balance = if (manifest.type == MiniAppConstants.TYPE_BLOCKCHAIN) "0 ETH" else null
                            )
                            installedApps.add(miniApp)
                        }
                        .onError { error ->
                            Log.e(TAG, "Failed to load manifest for $appId: $error")
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process miniapp: $appId", e)
                }
            }
            
            // 캐시 업데이트
            cache = CachedData(installedApps, System.currentTimeMillis())
            
            Log.d(TAG, "Scanned ${installedApps.size} apps successfully")
            MiniAppResult.Success(installedApps)
        } catch (e: Exception) {
            Log.e(TAG, "Scan failed", e)
            MiniAppResult.Error.ScanFailed(e)
        }
    }
    
    fun clearCache() {
        cache = null
    }
}