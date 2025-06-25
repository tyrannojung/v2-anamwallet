package com.anam145.wallet.feature.miniapp.common.domain.usecase

import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import javax.inject.Inject

/**
 * MiniApp Manifest를 로드하는 UseCase
 * 
 * 미니앱의 매니페스트 정보를 읽어옵니다.
 */
class LoadMiniAppManifestUseCase @Inject constructor(
    private val fileManager: MiniAppFileManager
) {
    /**
     * 지정된 앱의 매니페스트를 로드합니다.
     * 
     * @param appId 미니앱 ID
     * @return 매니페스트 로드 결과
     */
    suspend operator fun invoke(appId: String): MiniAppResult<MiniAppManifest> {
        return fileManager.loadManifest(appId)
    }
}