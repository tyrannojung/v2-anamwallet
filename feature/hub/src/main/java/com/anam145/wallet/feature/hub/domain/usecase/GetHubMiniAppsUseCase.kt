package com.anam145.wallet.feature.hub.domain.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.hub.remote.api.HubServerApi
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppScanner
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class HubMiniApp(
    val appId: String,
    val name: String,
    val version: String,
    val icon: String,  // iconUrl from server
    val type: String,
    val isInstalled: Boolean
)

/**
 * Hub에서 미니앱 목록을 가져오고 설치 여부를 확인하는 UseCase
 */
class GetHubMiniAppsUseCase @Inject constructor(
    private val hubApi: HubServerApi,
    private val scanner: MiniAppScanner
) {
    suspend operator fun invoke(): MiniAppResult<List<HubMiniApp>> {
        return try {
            coroutineScope {
                // 병렬로 서버 목록과 설치된 앱 목록 가져오기
                val serverAppsDeferred = async { 
                    hubApi.getMiniApps() 
                }
                val installedAppsDeferred = async { 
                    scanner.scanInstalledApps() 
                }
                
                val serverResponse = serverAppsDeferred.await()
                val installedAppsResult = installedAppsDeferred.await()
                
                // API 응답 확인
                if (!serverResponse.success) {
                    return@coroutineScope MiniAppResult.Error.UnknownError(
                        serverResponse.message
                    )
                }
                
                val serverApps = serverResponse.data ?: emptyList()
                
                // 설치된 앱 Map으로 변환 (O(1) 조회)
                val installedAppsMap = when (installedAppsResult) {
                    is MiniAppResult.Success -> installedAppsResult.data
                    is MiniAppResult.Error -> emptyMap()
                }
                
                // 서버 앱 목록에 설치 여부 추가
                val hubApps = serverApps.map { serverApp ->
                    HubMiniApp(
                        appId = serverApp.appId,
                        name = serverApp.name,
                        version = serverApp.version,
                        icon = serverApp.iconUrl ?: "",
                        type = serverApp.type,
                        isInstalled = installedAppsMap.containsKey(serverApp.appId)
                    )
                }
                
                MiniAppResult.Success(hubApps)
            }
        } catch (e: Exception) {
            MiniAppResult.Error.UnknownError(e.message ?: "Failed to load hub apps")
        }
    }
}