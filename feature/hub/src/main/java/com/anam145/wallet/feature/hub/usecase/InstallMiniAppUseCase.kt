package com.anam145.wallet.feature.hub.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InstallMiniAppUseCase @Inject constructor(
    private val miniAppRepository: MiniAppRepository,
)  {
    suspend operator fun invoke(miniApp : MiniApp) {
        miniAppRepository.insertMiniApp(miniApp)
    }
}