package com.anam145.wallet.feature.hub.di

import com.anam145.wallet.feature.hub.domain.repository.MiniAppManifestRepositoryImpl
import com.anam145.wallet.feature.hub.domain.repository.MiniAppRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class HubModule {

    @Binds
    abstract fun bindMiniAppRepository(
        miniAppRepository: MiniAppRepositoryImpl
    ): MiniAppRepositoryImpl

    @Binds
    abstract fun bindMiniAppManifestRepository(
        miniAppManifestRepository: MiniAppManifestRepositoryImpl
    ): MiniAppManifestRepositoryImpl


}