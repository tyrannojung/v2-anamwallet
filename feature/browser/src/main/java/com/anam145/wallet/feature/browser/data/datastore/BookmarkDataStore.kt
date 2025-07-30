package com.anam145.wallet.feature.browser.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anam145.wallet.feature.browser.domain.model.Bookmark
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.bookmarkDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bookmark_preferences"
)

/**
 * 북마크 데이터 저장소
 * DataStore를 사용하여 북마크 목록을 관리
 */
@Singleton
class BookmarkDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    companion object {
        private val BOOKMARKS_KEY = stringPreferencesKey("bookmarks")
    }
    
    /**
     * 북마크 목록을 Flow로 관찰
     */
    val bookmarks: Flow<List<Bookmark>> = context.bookmarkDataStore.data
        .map { preferences ->
            val bookmarksJson = preferences[BOOKMARKS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Bookmark>>(bookmarksJson)
            } catch (e: Exception) {
                emptyList()
            }
        }
    
    /**
     * 북마크 추가
     */
    suspend fun addBookmark(bookmark: Bookmark) {
        context.bookmarkDataStore.edit { preferences ->
            val currentBookmarksJson = preferences[BOOKMARKS_KEY] ?: "[]"
            val currentBookmarks = try {
                json.decodeFromString<List<Bookmark>>(currentBookmarksJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            // 중복 체크 (URL 기준)
            if (currentBookmarks.none { it.url == bookmark.url }) {
                currentBookmarks.add(0, bookmark) // 최신 북마크를 맨 앞에 추가
                preferences[BOOKMARKS_KEY] = json.encodeToString(currentBookmarks)
            }
        }
    }
    
    /**
     * 북마크 삭제
     */
    suspend fun deleteBookmark(bookmarkId: String) {
        context.bookmarkDataStore.edit { preferences ->
            val currentBookmarksJson = preferences[BOOKMARKS_KEY] ?: "[]"
            val currentBookmarks = try {
                json.decodeFromString<List<Bookmark>>(currentBookmarksJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            currentBookmarks.removeAll { it.id == bookmarkId }
            preferences[BOOKMARKS_KEY] = json.encodeToString(currentBookmarks)
        }
    }
    
    /**
     * URL로 북마크 삭제
     */
    suspend fun deleteBookmarkByUrl(url: String) {
        context.bookmarkDataStore.edit { preferences ->
            val currentBookmarksJson = preferences[BOOKMARKS_KEY] ?: "[]"
            val currentBookmarks = try {
                json.decodeFromString<List<Bookmark>>(currentBookmarksJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            currentBookmarks.removeAll { it.url == url }
            preferences[BOOKMARKS_KEY] = json.encodeToString(currentBookmarks)
        }
    }
    
    /**
     * 특정 URL이 북마크되어 있는지 확인
     */
    fun isBookmarked(url: String): Flow<Boolean> = bookmarks.map { bookmarkList ->
        bookmarkList.any { it.url == url }
    }
}