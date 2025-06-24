package com.anam145.wallet.feature.miniapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 테스트용 모듈
 * 
 * 멀티프로세스에서 Hilt가 제대로 작동하는지 확인하기 위한 임시 모듈입니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object TestModule {
    
    @Provides
    @Singleton
    fun provideTestString(): String {
        return "Hilt injection working in process: ${android.os.Process.myPid()}"
    }
}