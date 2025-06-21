package com.anam145.wallet.feature.settings.domain.usecase

import com.anam145.wallet.core.common.model.ThemeMode
import com.anam145.wallet.feature.settings.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 현재 테마 모드를 가져오는 UseCase
 * 
 * 비즈니스 규칙:
 * - 테마 설정을 Flow로 반환하여 실시간 변경 감지
 * - 기본값은 SYSTEM 테마
 */
class GetThemeModeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    operator fun invoke(): Flow<ThemeMode> = themeRepository.themeMode
}