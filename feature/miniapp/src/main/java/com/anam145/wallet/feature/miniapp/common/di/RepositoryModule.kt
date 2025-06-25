package com.anam145.wallet.feature.miniapp.common.di

import com.anam145.wallet.feature.miniapp.common.data.repository.BlockchainPreferencesRepositoryImpl
import com.anam145.wallet.feature.miniapp.common.domain.repository.BlockchainPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 의존성 주입 모듈
 * 
 * Repository 인터페이스와 구현체를 바인딩합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindBlockchainPreferencesRepository(
        impl: BlockchainPreferencesRepositoryImpl
    ): BlockchainPreferencesRepository
}