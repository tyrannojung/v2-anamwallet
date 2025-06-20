package com.anam145.wallet.feature.settings.domain.usecase

import com.anam145.wallet.core.ui.language.Language
import com.anam145.wallet.feature.settings.domain.repository.LanguageRepository
import javax.inject.Inject

/**
 * 언어를 변경하는 UseCase
 * 
 * 비즈니스 규칙:
 * - 언어 변경은 즉시 적용됨 (Activity 재시작 없이)
 * - 앱 재시작 후에도 유지되어야 함
 * - 지원하지 않는 언어는 설정할 수 없음
 */
class SetLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    suspend operator fun invoke(language: Language) {
        languageRepository.setLanguage(language)
    }
}