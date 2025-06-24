package com.anam145.wallet.feature.miniapp.data.common

import android.util.Log
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.core.common.data.MiniAppConstants
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

    /**
     * 설치된 미니앱들을 스캔하여 목록 반환
     *캐시 확인 → 5분 이내면 캐시된 데이터 반환
     *
     * @param forceRefresh true면 캐시 무시하고 강제로 새로 스캔
     * @return 스캔 결과 (성공 시 미니앱 목록, 실패 시 에러)
     */
    suspend fun scanInstalledApps(forceRefresh: Boolean = false): MiniAppResult<List<MiniApp>> = withContext(Dispatchers.IO) {
        try {
            // === 1. 캐시 확인 단계 ===
            val cached = cache
            if (!forceRefresh && cached != null && 
                System.currentTimeMillis() - cached.timestamp < MiniAppConstants.CACHE_DURATION_MS) {
                // 캐시가 유효한 경우 (강제 갱신 아님 + 캐시 존재 + 5분 이내)
                Log.d(TAG, "Returning cached apps: ${cached.apps.size}")
                return@withContext MiniAppResult.Success(cached.apps)
            }

            // === 2. 실제 스캔 시작 ===
            val installedApps = mutableListOf<MiniApp>()
            // 파일 시스템에서 설치된 앱 ID 목록 가져오기
            // 예: ["bitcoin", "ethereum", "gov24"]
            val appIds = fileManager.getInstalledApps()

            // 설치된 앱이 하나도 없는 경우
            if (appIds.isEmpty()) {
                return@withContext MiniAppResult.Error.NoAppsInstalled
            }
            
            Log.d(TAG, "Scanning ${appIds.size} installed apps")

            // === 3. 각 앱의 상세 정보 로드 ===
            appIds.forEach { appId ->
                try {
                    // manifest.json 파일 읽기 시도
                    fileManager.loadManifest(appId)
                        .onSuccess { manifest ->
                            val miniApp = MiniApp(
                                appId = manifest.appId,
                                name = manifest.name,
                                // manifest의 type 문자열을 enum으로 변환
                                type = when (manifest.type) {
                                    MiniAppConstants.TYPE_BLOCKCHAIN -> MiniAppType.BLOCKCHAIN
                                    else -> MiniAppType.APP
                                },
                                // 아이콘 파일 경로 생성
                                // 예: /data/data/.../files/miniapps/bitcoin/assets/icons/app_icon.png
                                iconPath = fileManager.getMiniAppBasePath(appId) + MiniAppConstants.ICON_PATH,
                                // 블록체인 타입만 잔액 표시 (현재 하드 코딩 상태)
                                balance = if (manifest.type == MiniAppConstants.TYPE_BLOCKCHAIN) "0 ETH" else null
                            )
                            installedApps.add(miniApp)
                        }
                        // manifest 로드 실패 시 로그만 남기고 계속 진행
                        // (하나의 앱이 실패해도 다른 앱들은 처리)
                        .onError { error ->
                            Log.e(TAG, "Failed to load manifest for $appId: $error")
                        }
                } catch (e: Exception) {
                    // 예외 발생 시에도 다른 앱 처리 계속
                    Log.e(TAG, "Failed to process miniapp: $appId", e)
                }
            }

            // === 4. 캐시 업데이트 ===
            // 스캔 결과를 현재 시간과 함께 캐시에 저장
            cache = CachedData(installedApps, System.currentTimeMillis())

            // === 5. 결과 반환 ===
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