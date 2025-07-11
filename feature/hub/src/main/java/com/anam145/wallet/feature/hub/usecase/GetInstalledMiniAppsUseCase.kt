package com.anam145.wallet.feature.hub.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetInstalledMiniAppsUseCase @Inject constructor(
    private val miniAppRepository: MiniAppRepository
)  {
    operator fun invoke(): Flow<List<MiniApp>> = miniAppRepository.getMiniApps()
}