package com.anam145.wallet.feature.settings.domain.repository

import com.anam145.wallet.core.ui.language.Language
import com.anam145.wallet.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Settings Repository 인터페이스
 * 
 * 설정 관련 데이터 작업을 정의합니다.
 */
interface SettingsRepository {
    
    /**
     * 현재 테마 모드를 가져옵니다
     */
    val themeMode: Flow<ThemeMode>
    
    /**
     * 현재 언어 설정을 가져옵니다
     */
    val language: Flow<Language>
    
    /**
     * 테마 모드를 변경합니다
     */
    suspend fun setThemeMode(themeMode: ThemeMode)
    
    /**
     * 언어를 변경합니다
     */
    suspend fun setLanguage(language: Language)
}