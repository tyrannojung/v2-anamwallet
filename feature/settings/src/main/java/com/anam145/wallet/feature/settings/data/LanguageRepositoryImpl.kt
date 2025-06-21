package com.anam145.wallet.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.anam145.wallet.core.common.model.Language
import com.anam145.wallet.feature.settings.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LanguageRepository 구현체
 * 
 * DataStore를 사용하여 언어 설정을 영구 저장합니다.
 */
@Singleton
class LanguageRepositoryImpl @Inject constructor(
    // SharedPreferences의 현대적 대체제
    // 비동기적으로 key-value 데이터 저장
    private val dataStore: DataStore<Preferences>
) : LanguageRepository {
    
    companion object {
        // "language"라는 이름의 String 타입 키 생성
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }
    
    override val language: Flow<Language> = dataStore.data.map { preferences ->
        // 1. 저장된 언어 코드 읽기 (없으면 한국어)
        val languageCode = preferences[LANGUAGE_KEY] ?: Language.KOREAN.code
        // find 동작 과정:
        // 1. Language.KOREAN.code == "en"? → "ko" == "en" → false
        // 2. Language.ENGLISH.code == "en"? → "en" == "en" → true! (찾았다!)
        // 결과: Language.ENGLISH 반환
        Language.entries.find { it.code == languageCode } ?: Language.KOREAN
    }
    
    override suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }
}