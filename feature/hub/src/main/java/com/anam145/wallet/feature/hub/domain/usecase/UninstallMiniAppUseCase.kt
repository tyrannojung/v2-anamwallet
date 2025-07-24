package com.anam145.wallet.feature.hub.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.core.data.datastore.SkinDataStore
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppScanner
import javax.inject.Inject

/**
 * 미니앱을 제거하는 UseCase
 */
class UninstallMiniAppUseCase @Inject constructor(
    private val fileManager: MiniAppFileManager,
    private val scanner: MiniAppScanner,
    private val skinDataStore: SkinDataStore
) {
    suspend operator fun invoke(appId: String): MiniAppResult<Unit> {
        return try {
            // 1. 현재 스킨에서 앱 제거
            skinDataStore.removeAppFromCurrentSkin(appId)
            
            // 2. 다른 스킨에서도 사용 중인지 확인
            val isUsedByOtherSkin = skinDataStore.isAppUsedByAnySkin(appId)
            
            if (!isUsedByOtherSkin) {
                // 3. 아무도 사용하지 않으면 파일 삭제
                val result = fileManager.uninstallMiniApp(appId)
                
                // 삭제 실패 시 early return
                if (result is MiniAppResult.Error) {
                    // 실패했으므로 현재 스킨에 다시 추가
                    skinDataStore.addAppToCurrentSkin(appId)
                    return result
                }
            }
            
            // 4. 캐시 초기화하여 UI 업데이트
            scanner.clearCache()
            
            MiniAppResult.Success(Unit)
        } catch (e: Exception) {
            MiniAppResult.Error.UnknownError(e.message ?: "Uninstall failed")
        }
    }
}