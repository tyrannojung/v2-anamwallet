package com.anam145.wallet.feature.miniapp.di

import com.anam145.wallet.feature.miniapp.data.repository.MiniAppRepositoryImpl
import com.anam145.wallet.feature.miniapp.domain.repository.MiniAppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MiniAppModule {
    
    @Binds
    @Singleton
    abstract fun bindMiniAppRepository(
        miniAppRepositoryImpl: MiniAppRepositoryImpl
    ): MiniAppRepository
}