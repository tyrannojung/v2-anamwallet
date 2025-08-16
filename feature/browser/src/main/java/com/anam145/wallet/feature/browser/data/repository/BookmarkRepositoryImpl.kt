package com.anam145.wallet.feature.browser.data.repository

import com.anam145.wallet.feature.browser.data.datastore.BookmarkDataStore
import com.anam145.wallet.feature.browser.domain.model.Bookmark
import com.anam145.wallet.feature.browser.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * BookmarkRepository 구현체
 */
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDataStore: BookmarkDataStore
) : BookmarkRepository {
    
    override fun getBookmarks(): Flow<List<Bookmark>> = bookmarkDataStore.bookmarks
    
    override suspend fun addBookmark(bookmark: Bookmark) {
        bookmarkDataStore.addBookmark(bookmark)
    }
    
    override suspend fun deleteBookmark(bookmarkId: String) {
        bookmarkDataStore.deleteBookmark(bookmarkId)
    }
    
    override suspend fun deleteBookmarkByUrl(url: String) {
        bookmarkDataStore.deleteBookmarkByUrl(url)
    }
    
    override fun isBookmarked(url: String): Flow<Boolean> = bookmarkDataStore.isBookmarked(url)
}