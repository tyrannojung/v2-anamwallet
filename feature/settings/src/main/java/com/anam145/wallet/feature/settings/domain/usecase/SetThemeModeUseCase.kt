package com.anam145.wallet.feature.settings.domain.usecase

import com.anam145.wallet.core.common.model.ThemeMode
import com.anam145.wallet.feature.settings.domain.repository.ThemeRepository
import javax.inject.Inject

/**
 * 테마 모드를 변경하는 UseCase
 * 
 * 비즈니스 규칙:
 * - 테마 변경은 즉시 적용됨
 * - 앱 재시작 후에도 유지되어야 함
 */
class SetThemeModeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    suspend operator fun invoke(themeMode: ThemeMode) {
        themeRepository.setThemeMode(themeMode)
    }
}