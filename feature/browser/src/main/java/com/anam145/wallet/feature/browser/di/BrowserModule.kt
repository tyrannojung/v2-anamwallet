package com.anam145.wallet.feature.browser.di

import com.anam145.wallet.feature.browser.data.repository.BookmarkRepositoryImpl
import com.anam145.wallet.feature.browser.data.repository.UniversalRepositoryImpl
import com.anam145.wallet.feature.browser.domain.repository.BookmarkRepository
import com.anam145.wallet.feature.browser.domain.repository.UniversalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Browser 모듈의 Hilt DI 설정
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BrowserModule {
    
    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl
    ): BookmarkRepository
    
    @Binds
    @Singleton
    abstract fun bindUniversalRepository(
        universalRepositoryImpl: UniversalRepositoryImpl
    ): UniversalRepository
}