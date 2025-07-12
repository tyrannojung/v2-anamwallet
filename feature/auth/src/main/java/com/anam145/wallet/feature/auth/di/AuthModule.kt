package com.anam145.wallet.feature.auth.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Auth 모듈의 의존성 주입 설정
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    // PasswordManager는 @Singleton으로 자동 주입되므로 별도 제공 메서드 불필요
}