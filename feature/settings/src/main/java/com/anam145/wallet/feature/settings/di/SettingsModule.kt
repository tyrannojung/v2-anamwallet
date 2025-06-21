package com.anam145.wallet.feature.settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.anam145.wallet.feature.settings.domain.repository.ThemeRepository
import com.anam145.wallet.feature.settings.domain.repository.LanguageRepository
import com.anam145.wallet.feature.settings.data.ThemeRepositoryImpl
import com.anam145.wallet.feature.settings.data.LanguageRepositoryImpl
import dagger.Binds

// DataStore 인스턴스를 위한 확장 속성
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_preferences"
)

/**
 * Settings 모듈의 의존성 주입 설정
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    
    @Binds
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository
    
    @Binds
    abstract fun bindLanguageRepository(
        languageRepositoryImpl: LanguageRepositoryImpl
    ): LanguageRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideSettingsDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> {
            return context.settingsDataStore
        }
    }
}