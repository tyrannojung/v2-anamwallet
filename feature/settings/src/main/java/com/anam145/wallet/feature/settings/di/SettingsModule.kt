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
import com.anam145.wallet.feature.settings.domain.repository.LanguageRepository
import com.anam145.wallet.feature.settings.data.LanguageRepositoryImpl
import dagger.Binds

// DataStore 인스턴스를 위한 확장 속성
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_preferences"
)

/**
 * 각 feature가 자신만의 DI 설정을 가짐
 * Settings 모듈의 의존성 주입 설정
 * SettingsModule은 설정 파일
 * Hilt에게 "이렇게 객체를 만들어라"고 알려주는 곳
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    /**
     * Repository 바인딩
     * 의미: "누군가 LanguageRepository를 요청하면, LanguageRepositoryImpl을 줘라"
     * 왜 필요?: ViewModel은 인터페이스(LanguageRepository)만 알고, 구현체는 모름
     * */
    @Binds
    abstract fun bindLanguageRepository(
        languageRepositoryImpl: LanguageRepositoryImpl
    ): LanguageRepository

    /**
     * DataStore 제공
     * DataStore가 필요하면 이렇게 만들어라
     * @Singleton: 앱 전체에서 하나만 존재
     * */
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