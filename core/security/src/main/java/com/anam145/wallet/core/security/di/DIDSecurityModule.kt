package com.anam145.wallet.core.security.di

import android.content.Context
import com.anam145.wallet.core.security.keystore.DIDKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DID 보안 관련 Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DIDSecurityModule {
    
    @Provides
    @Singleton
    fun provideDIDKeyManager(
        @ApplicationContext context: Context
    ): DIDKeyManager {
        return DIDKeyManager(context)
    }
}