package com.anam145.wallet.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.anam145.wallet.core.ui.language.Language
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
    private val dataStore: DataStore<Preferences>
) : LanguageRepository {
    
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }
    
    override val language: Flow<Language> = dataStore.data.map { preferences ->
        val languageCode = preferences[LANGUAGE_KEY] ?: Language.KOREAN.code
        Language.entries.find { it.code == languageCode } ?: Language.KOREAN
    }
    
    override suspend fun setLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }
}