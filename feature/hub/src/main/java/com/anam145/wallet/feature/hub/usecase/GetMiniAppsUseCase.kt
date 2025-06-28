package com.anam145.wallet.feature.hub.usecase

import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject


class GetMiniAppsUseCase @Inject constructor(
)  {
    fun invoke(): Flow<List<MiniApp>> = emptyFlow()
}