package com.anam145.wallet.feature.hub.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepository
import com.anam145.wallet.feature.hub.remote.api.HubServerApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUnInstalledMiniAppsUseCase @Inject constructor(
    private val hubClient: HubServerApi
) {
    operator fun invoke(): Flow<List<MiniApp>> = flow {
        try {
            val result = hubClient.getMiniAppsList()
            emit(result)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
}