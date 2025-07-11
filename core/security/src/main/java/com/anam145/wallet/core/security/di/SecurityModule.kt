package com.anam145.wallet.core.security.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.anam145.wallet.core.security.data.util.KdfParamsTypeAdapter
import com.anam145.wallet.core.security.model.KdfParams
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Security DataStore 확장 프로퍼티
 */
private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "security_preferences"
)

/**
 * Security 모듈 의존성 제공
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(KdfParams::class.java, KdfParamsTypeAdapter())
            .setPrettyPrinting()
            .create()
    }
    
    @Provides
    @Singleton
    @Named("security")
    fun provideSecurityDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.securityDataStore
    }
}