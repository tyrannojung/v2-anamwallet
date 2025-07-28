package com.anam145.wallet.feature.identity.di

import com.anam145.wallet.feature.identity.data.remote.DIDApiService
import com.anam145.wallet.feature.identity.data.repository.DIDRepositoryImpl
import com.anam145.wallet.feature.identity.domain.repository.DIDRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Identity 기능 관련 Hilt 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class IdentityModule {
    
    @Binds
    abstract fun bindDIDRepository(
        impl: DIDRepositoryImpl
    ): DIDRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideDIDRetrofit(): Retrofit {
            // 에뮬레이터용 DID 서버 주소
            return Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8081/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        
        @Provides
        @Singleton
        fun provideDIDApiService(retrofit: Retrofit): DIDApiService {
            return retrofit.create(DIDApiService::class.java)
        }
    }
}