package com.anam145.wallet.feature.hub.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMiniAppManifestsUseCase @Inject constructor(
    private val miniAppManifestRepository: MiniAppRepository
)  {
    operator fun invoke(): Flow<List<MiniApp>> = miniAppManifestRepository.getMiniApps()
}