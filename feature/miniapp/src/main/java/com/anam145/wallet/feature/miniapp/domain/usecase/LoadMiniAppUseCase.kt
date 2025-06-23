package com.anam145.wallet.feature.miniapp.domain.usecase

import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.domain.repository.MiniAppRepository
import javax.inject.Inject

class LoadMiniAppUseCase @Inject constructor(
    private val repository: MiniAppRepository
) {
    suspend operator fun invoke(appId: String): MiniAppResult<MiniAppManifest> {
        return repository.loadMiniAppManifest(appId)
    }
}