package com.anam145.wallet.feature.hub.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppScanner
import javax.inject.Inject

/**
 * 미니앱을 제거하는 UseCase
 */
class UninstallMiniAppUseCase @Inject constructor(
    private val fileManager: MiniAppFileManager,
    private val scanner: MiniAppScanner
) {
    suspend operator fun invoke(appId: String): MiniAppResult<Unit> {
        return try {
            // 1. 미니앱 제거
            val result = fileManager.uninstallMiniApp(appId)
            
            // 2. 제거 성공 시 캐시 초기화
            if (result is MiniAppResult.Success) {
                scanner.clearCache()
            }
            
            result
        } catch (e: Exception) {
            MiniAppResult.Error.UnknownError(e.message ?: "Uninstall failed")
        }
    }
}