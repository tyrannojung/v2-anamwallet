package com.anam145.wallet.feature.hub.domain.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.core.data.datastore.SkinDataStore
import com.anam145.wallet.feature.hub.remote.api.HubServerApi
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppScanner
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
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
    private val scanner: MiniAppScanner,
    private val skinDataStore: SkinDataStore
) {
    suspend operator fun invoke(): MiniAppResult<List<HubMiniApp>> {
        return try {
            coroutineScope {
                // 병렬로 서버 목록, 설치된 앱 목록, 현재 스킨의 앱 목록 가져오기
                val serverAppsDeferred = async { 
                    hubApi.getMiniApps() 
                }
                val installedAppsDeferred = async { 
                    scanner.scanInstalledApps() 
                }
                val currentSkinDeferred = async {
                    val skin = skinDataStore.selectedSkin.first()
                    skinDataStore.getAppsForSkin(skin)
                }
                
                val serverResponse = serverAppsDeferred.await()
                val installedAppsResult = installedAppsDeferred.await()
                val currentSkinApps = currentSkinDeferred.await()
                
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
                // 설치됨 = 파일이 존재하고 AND 현재 스킨의 리스트에 포함됨
                val hubApps = serverApps.map { serverApp ->
                    val isPhysicallyInstalled = installedAppsMap.containsKey(serverApp.appId)
                    val isInCurrentSkin = currentSkinApps.contains(serverApp.appId)
                    
                    HubMiniApp(
                        appId = serverApp.appId,
                        name = serverApp.name,
                        version = serverApp.version,
                        icon = serverApp.iconUrl ?: "",
                        type = serverApp.type,
                        isInstalled = isPhysicallyInstalled && isInCurrentSkin
                    )
                }
                
                MiniAppResult.Success(hubApps)
            }
        } catch (e: Exception) {
            MiniAppResult.Error.UnknownError(e.message ?: "Failed to load hub apps")
        }
    }
}