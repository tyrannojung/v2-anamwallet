package com.anam145.wallet.feature.miniapp.common.data.common

import android.util.Log
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.core.common.data.MiniAppConstants
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.core.common.result.onSuccess
import com.anam145.wallet.core.common.result.onError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
        val apps: Map<String, MiniApp>,
        val timestamp: Long
    )
    
    private var cache: CachedData? = null
    
    /**
     * 캐시 접근 동기화를 위한 Mutex
     * 
     * Mutex(Mutual Exclusion)는 코루틴 환경에서 공유 자원에 대한 동시 접근을 제어하는 동기화 도구입니다.
     * 
     * 주요 특징:
     * - 한 번에 하나의 코루틴만 Mutex로 보호된 영역에 접근 가능
     * - withLock {} 블록을 사용하여 자동으로 lock/unlock 처리
     * - 코루틴이 대기 중일 때 스레드를 블로킹하지 않고 suspend됨
     * 
     * 이 Mutex가 보호하는 작업:
     * 1. 캐시 읽기 (scanInstalledApps에서 캐시 확인)
     * 2. 캐시 쓰기 (스캔 결과를 캐시에 저장)
     * 3. 캐시 초기화 (clearCache에서 null로 설정)
     * 
     * 동시성 문제 예시:
     * - Thread A가 캐시를 업데이트하는 중에 Thread B가 읽으면 불완전한 데이터를 읽을 수 있음
     * - 여러 스레드가 동시에 캐시를 업데이트하면 일부 업데이트가 손실될 수 있음
     */
    private val cacheMutex = Mutex()

    /**
     * 설치된 미니앱들을 스캔하여 Map으로 반환
     * 캐시 확인 → 5분 이내면 캐시된 데이터 반환
     *
     * @param forceRefresh true면 캐시 무시하고 강제로 새로 스캔
     * @return 스캔 결과 (성공 시 미니앱 Map<appId, MiniApp>, 실패 시 에러)
     */
    suspend fun scanInstalledApps(forceRefresh: Boolean = false): MiniAppResult<Map<String, MiniApp>> = withContext(Dispatchers.IO) {
        try {
            // === 1. 캐시 확인 단계 ===
            /**
             * Mutex를 사용한 캐시 읽기 보호
             * 
             * withLock 블록:
             * - Mutex lock을 획득한 후 코드 실행
             * - 블록이 끝나면 자동으로 unlock (예외 발생 시에도 보장)
             * - 다른 코루틴이 이미 lock을 가지고 있다면 대기
             * 
             * 동기화가 필요한 이유:
             * - cache 변수를 읽는 동안 다른 스레드가 동시에 수정하는 것을 방지
             * - cache?.apps와 cache?.timestamp를 읽는 사이에 cache가 변경되는 것을 방지
             */
            cacheMutex.withLock {
                val cached = cache
                if (!forceRefresh && cached != null && 
                    System.currentTimeMillis() - cached.timestamp < MiniAppConstants.CACHE_DURATION_MS) {
                    // 캐시가 유효한 경우 (강제 갱신 아님 + 캐시 존재 + 5분 이내)
                    Log.d(TAG, "Returning cached apps: ${cached.apps.size}")
                    return@withContext MiniAppResult.Success(cached.apps)
                }
            }

            // === 2. 실제 스캔 시작 ===
            val installedAppsMap = mutableMapOf<String, MiniApp>()
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
                                iconPath = fileManager.getMiniAppBasePath(appId) + MiniAppConstants.ICON_PATH
                            )
                            installedAppsMap[appId] = miniApp
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
            /**
             * Mutex를 사용한 캐시 쓰기 보호
             * 
             * 캐시 업데이트 시 동기화가 중요한 이유:
             * 1. 원자성(Atomicity) 보장: CachedData 객체 생성과 cache 변수 할당이 하나의 원자적 작업으로 처리
             * 2. 가시성(Visibility) 보장: 한 스레드의 변경사항이 다른 스레드에 즉시 보임
             * 3. 순서 보장: 여러 스레드가 동시에 업데이트를 시도해도 순차적으로 처리
             * 
             * Mutex 없이 발생할 수 있는 문제:
             * - Thread A와 B가 동시에 스캔 후 캐시 업데이트 시도
             * - 둘 중 하나의 업데이트가 손실되거나 불완전한 상태로 저장될 수 있음
             */
            cacheMutex.withLock {
                cache = CachedData(installedAppsMap, System.currentTimeMillis())
            }

            // === 5. 결과 반환 ===
            Log.d(TAG, "Scanned ${installedAppsMap.size} apps successfully")
            MiniAppResult.Success(installedAppsMap)
        } catch (e: Exception) {
            Log.e(TAG, "Scan failed", e)
            MiniAppResult.Error.ScanFailed(e)
        }
    }
    
    /**
     * 빠른 설치 여부 확인
     * @param appId 확인할 앱 ID
     * @return 설치 여부
     */
    suspend fun isInstalled(appId: String): Boolean {
        return when (val result = scanInstalledApps()) {
            is MiniAppResult.Success -> result.data.containsKey(appId)
            is MiniAppResult.Error -> false
        }
    }
    
    // 설치된 앱 변경 이벤트를 위한 Flow
    // replay = 1로 설정하여 구독자가 나중에 연결되어도 마지막 이벤트를 받을 수 있도록 함
    private val _appsChangedEvent = MutableSharedFlow<Unit>(replay = 1)
    val appsChangedEvent: SharedFlow<Unit> = _appsChangedEvent.asSharedFlow()
    
    /**
     * 캐시 초기화
     * 
     * suspend 함수로 변경된 이유:
     * - Mutex.withLock은 suspend 함수이므로 clearCache도 suspend 함수여야 함
     * - 이미 다른 코루틴이 캐시를 사용 중이면 대기해야 하기 때문
     * 
     * 동작 순서:
     * 1. Mutex lock 획득 (다른 코루틴이 사용 중이면 대기)
     * 2. cache를 null로 설정 (안전하게 동기화된 상태에서)
     * 3. Mutex unlock (자동)
     * 4. 변경 이벤트 발생 (UI 업데이트 트리거)
     * 
     * 주의: tryEmit은 Mutex 밖에서 호출
     * - 이벤트 발생은 빠르게 처리되므로 lock을 오래 잡을 필요 없음
     * - 이벤트 처리가 실패해도 캐시 초기화는 성공해야 함
     */
    suspend fun clearCache() {
        cacheMutex.withLock {
            cache = null
        }
        // 변경 이벤트 발생
        _appsChangedEvent.tryEmit(Unit)
    }
}