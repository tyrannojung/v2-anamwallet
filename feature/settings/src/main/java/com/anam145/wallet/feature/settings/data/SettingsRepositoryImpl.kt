package com.anam145.wallet.feature.settings.data

import com.anam145.wallet.core.ui.language.Language
import com.anam145.wallet.feature.settings.domain.model.ThemeMode
import com.anam145.wallet.feature.settings.domain.repository.SettingsRepository
import com.anam145.wallet.feature.settings.domain.repository.ThemeRepository
import com.anam145.wallet.feature.settings.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Settings Repository 구현체
 * 
 * ThemeRepository와 LanguageRepository를 통합하여
 * 기존 SettingsRepository 인터페이스를 유지합니다.
 * 
 * 이는 다른 모듈과의 호환성을 위해 임시로 유지합니다.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val languageRepository: LanguageRepository
) : SettingsRepository {
    
    /**
     * 현재 테마 모드를 가져옵니다
     */
    override val themeMode: Flow<ThemeMode> = themeRepository.themeMode
    
    /**
     * 현재 언어 설정을 가져옵니다
     */
    override val language: Flow<Language> = languageRepository.language
    
    /**
     * 테마 모드를 변경합니다
     */
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        themeRepository.setThemeMode(themeMode)
    }
    
    /**
     * 언어를 변경합니다
     */
    override suspend fun setLanguage(language: Language) {
        languageRepository.setLanguage(language)
    }
}