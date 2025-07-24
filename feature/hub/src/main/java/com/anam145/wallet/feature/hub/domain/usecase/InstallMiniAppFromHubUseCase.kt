package com.anam145.wallet.feature.hub.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.core.data.datastore.SkinDataStore
import com.anam145.wallet.feature.hub.remote.api.HubServerApi
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppScanner
import javax.inject.Inject

/**
 * Hub에서 미니앱을 다운로드하고 설치하는 UseCase
 */
class InstallMiniAppFromHubUseCase @Inject constructor(
    private val hubApi: HubServerApi,
    private val fileManager: MiniAppFileManager,
    private val scanner: MiniAppScanner,
    private val skinDataStore: SkinDataStore
) {
    suspend operator fun invoke(appId: String): MiniAppResult<Unit> {
        return try {
            // 1. 파일이 이미 설치되어 있는지 확인
            val isAlreadyInstalled = scanner.isInstalled(appId)
            
            if (!isAlreadyInstalled) {
                // 2. 새 앱: 서버에서 다운로드
                val response = hubApi.downloadMiniApp(appId)
                
                if (!response.isSuccessful) {
                    val errorMessage = when (response.code()) {
                        404 -> "App not found on server"
                        403 -> "Access denied"
                        500, 502, 503 -> "Server error, please try again later"
                        else -> "Download failed (${response.code()})"
                    }
                    return MiniAppResult.Error.UnknownError(errorMessage)
                }
                
                // 3. InputStream으로 설치 (use로 자동 close 보장)
                response.body()?.byteStream()?.use { stream ->
                    val result = fileManager.installFromInputStream(appId, stream)
                    
                    // 설치 실패 시 early return
                    if (result is MiniAppResult.Error) {
                        return result
                    }
                } ?: return MiniAppResult.Error.UnknownError("Empty response body")
            }
            
            // 4. 현재 스킨에 앱 추가 (이미 설치되어 있거나 새로 설치한 경우 모두)
            skinDataStore.addAppToCurrentSkin(appId)
            
            // 5. 캐시 초기화하여 UI 업데이트
            scanner.clearCache()
            
            MiniAppResult.Success(Unit)
            
        } catch (e: java.net.UnknownHostException) {
            MiniAppResult.Error.UnknownError("No internet connection")
        } catch (e: java.net.SocketTimeoutException) {
            MiniAppResult.Error.UnknownError("Connection timeout, please try again")
        } catch (e: Exception) {
            MiniAppResult.Error.UnknownError("Install failed: ${e.message}")
        }
    }
}