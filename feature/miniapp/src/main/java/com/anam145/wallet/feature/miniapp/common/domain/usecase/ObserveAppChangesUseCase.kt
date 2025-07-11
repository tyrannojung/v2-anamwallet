package com.anam145.wallet.feature.miniapp.common.domain.usecase

import com.anam145.wallet.feature.miniapp.common.domain.repository.MiniAppRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * 미니앱 변경 이벤트를 관찰하는 UseCase
 * 
 * 미니앱이 설치되거나 삭제될 때 이벤트를 발생시켜
 * UI가 자동으로 새로고침되도록 합니다.
 */
class ObserveAppChangesUseCase @Inject constructor(
    private val repository: MiniAppRepository
) {
    operator fun invoke(): SharedFlow<Unit> = repository.observeAppChanges()
}