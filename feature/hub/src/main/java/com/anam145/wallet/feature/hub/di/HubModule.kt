package com.anam145.wallet.feature.hub.di

import com.anam145.wallet.feature.hub.remote.api.HubServerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HubModule {

    private const val HUB_BASE_URL = "https://anamhub.xyz/"

    @Provides
    @Singleton
    fun provideHubApi(): HubServerApi {
        return Retrofit.Builder()
            .baseUrl(HUB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HubServerApi::class.java)
    }
}