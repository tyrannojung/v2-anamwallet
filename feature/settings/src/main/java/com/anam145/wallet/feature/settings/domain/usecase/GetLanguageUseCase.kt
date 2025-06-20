package com.anam145.wallet.feature.settings.domain.usecase

import com.anam145.wallet.core.ui.language.Language
import com.anam145.wallet.feature.settings.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 현재 언어 설정을 가져오는 UseCase
 * 
 * 비즈니스 규칙:
 * - 언어 설정을 Flow로 반환하여 실시간 변경 감지
 * - 기본값은 한국어
 */
class GetLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    operator fun invoke(): Flow<Language> = languageRepository.language
}