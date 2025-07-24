package com.anam145.wallet.core.data.di

import android.content.Context
import com.anam145.wallet.core.data.datastore.MiniAppDataStore
import com.anam145.wallet.core.data.datastore.SkinDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideMiniAppDataStore(
        @ApplicationContext context: Context
    ): MiniAppDataStore {
        return MiniAppDataStore(context)
    }
    
    @Provides
    @Singleton
    fun provideSkinDataStore(
        @ApplicationContext context: Context
    ): SkinDataStore {
        return SkinDataStore(context)
    }
}