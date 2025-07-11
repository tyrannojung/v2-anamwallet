package com.anam145.wallet.feature.miniapp.common.domain.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.repository.MiniAppRepository
import javax.inject.Inject

class GetInstalledMiniAppsUseCase @Inject constructor(
    private val repository: MiniAppRepository
) {
    suspend operator fun invoke(): MiniAppResult<Map<String, MiniApp>> {
        return repository.getInstalledMiniApps()
    }
}