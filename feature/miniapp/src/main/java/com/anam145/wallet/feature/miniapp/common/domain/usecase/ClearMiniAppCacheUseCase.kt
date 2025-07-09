package com.anam145.wallet.feature.miniapp.common.domain.usecase

import com.anam145.wallet.feature.miniapp.common.domain.repository.MiniAppRepository
import javax.inject.Inject

/**
 * 미니앱 캐시를 클리어하는 UseCase
 * 
 * Hub에서 새로운 앱을 설치하거나 업데이트한 후,
 * 미니앱 목록을 새로고침하기 위해 사용됩니다.
 */
class ClearMiniAppCacheUseCase @Inject constructor(
    private val repository: MiniAppRepository
) {
    suspend operator fun invoke() {
        repository.clearCache()
    }
}