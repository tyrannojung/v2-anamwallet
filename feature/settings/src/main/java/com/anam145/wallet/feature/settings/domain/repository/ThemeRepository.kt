package com.anam145.wallet.feature.settings.domain.repository

import com.anam145.wallet.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * 테마 관련 Repository 인터페이스
 * 
 * 테마 설정의 저장 및 조회를 담당합니다.
 */
interface ThemeRepository {
    
    /**
     * 현재 테마 모드를 가져옵니다
     */
    val themeMode: Flow<ThemeMode>
    
    /**
     * 테마 모드를 변경합니다
     */
    suspend fun setThemeMode(themeMode: ThemeMode)
}