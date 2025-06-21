package com.anam145.wallet.core.common.util

import com.anam145.wallet.core.common.model.Language
import java.util.Locale

/**
 * 언어 관련 유틸리티 함수들
 */
object LanguageUtil {
    /**
     * 시스템 언어를 지원하는 언어로 변환
     */
    fun getSystemLanguage(): Language {
        return when (Locale.getDefault().language) {
            "ko" -> Language.KOREAN
            "en" -> Language.ENGLISH
            else -> Language.KOREAN  // 지원하지 않는 언어는 한국어로 기본 설정
        }
    }
}