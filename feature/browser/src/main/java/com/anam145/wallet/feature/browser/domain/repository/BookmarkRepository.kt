package com.anam145.wallet.feature.browser.domain.repository

import com.anam145.wallet.feature.browser.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

/**
 * 북마크 Repository 인터페이스
 */
interface BookmarkRepository {
    /**
     * 모든 북마크 조회
     */
    fun getBookmarks(): Flow<List<Bookmark>>
    
    /**
     * 북마크 추가
     */
    suspend fun addBookmark(bookmark: Bookmark)
    
    /**
     * 북마크 삭제
     */
    suspend fun deleteBookmark(bookmarkId: String)
    
    /**
     * URL로 북마크 삭제
     */
    suspend fun deleteBookmarkByUrl(url: String)
    
    /**
     * 특정 URL이 북마크되어 있는지 확인
     */
    fun isBookmarked(url: String): Flow<Boolean>
}