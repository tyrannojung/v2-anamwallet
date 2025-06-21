package com.anam145.wallet.feature.settings.domain.repository

import com.anam145.wallet.core.common.model.Language
import kotlinx.coroutines.flow.Flow

/**
 * 언어 관련 Repository 인터페이스
 * 
 * 언어 설정의 저장 및 조회를 담당합니다.
 */
interface LanguageRepository {
    
    /**
     * 현재 언어 설정을 가져옵니다
     */
    val language: Flow<Language>
    
    /**
     * 언어를 변경합니다
     */
    suspend fun setLanguage(language: Language)
}