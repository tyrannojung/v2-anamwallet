package com.anam145.wallet.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.anam145.wallet.core.common.model.ThemeMode
import com.anam145.wallet.feature.settings.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThemeRepository 구현체
 * 
 * DataStore를 사용하여 테마 설정을 영구 저장함.
 */
@Singleton // 앱 전체에서 인스턴스 하나만
class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences> // DataStore 주입
) : ThemeRepository {

    // static 변수처럼 클래스 레벨에서 공유
    companion object {
        // "theme_mode"라는 키로 문자열 저장
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
    
    override val themeMode: Flow<ThemeMode> = dataStore.data
        .map { preferences -> // Preferences 객체를 ThemeMode로 변환
        val themeName = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        // 1. preferences에서 "theme_mode" 키의 값 읽기
        // 2. 없으면 "SYSTEM" 사용
        // "DARK" → ThemeMode.DARK (문자열 → enum)
        ThemeMode.valueOf(themeName)
    }
    
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            // ThemeMode.DARK → "DARK" (enum → 문자열)
            // DataStore는  Coroutine Flow를 자체적으로 지원함
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }
}