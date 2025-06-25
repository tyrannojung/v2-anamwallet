package com.anam145.wallet.feature.miniapp.common.domain.usecase

import com.anam145.wallet.feature.miniapp.common.domain.repository.MiniAppRepository
import javax.inject.Inject

/**
 * MiniApp 초기화 상태를 확인하는 UseCase
 * 
 * 현재 MiniApp들이 초기화되었는지 여부를 반환합니다.
 */
class CheckInitializationStateUseCase @Inject constructor(
    private val repository: MiniAppRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.isMiniAppsInitialized()
    }
}