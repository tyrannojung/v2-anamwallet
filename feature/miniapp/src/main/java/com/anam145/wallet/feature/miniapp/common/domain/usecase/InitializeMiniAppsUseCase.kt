package com.anam145.wallet.feature.miniapp.common.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.repository.MiniAppRepository
import javax.inject.Inject

class InitializeMiniAppsUseCase @Inject constructor(
    private val repository: MiniAppRepository
) {
    suspend operator fun invoke(): MiniAppResult<Unit> {
        return repository.initializeMiniApps()
    }
}