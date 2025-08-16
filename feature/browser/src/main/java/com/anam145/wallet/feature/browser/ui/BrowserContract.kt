package com.anam145.wallet.feature.browser.ui

import com.anam145.wallet.feature.browser.domain.model.Bookmark

/**
 * Browser 화면의 MVI Contract
 */
interface BrowserContract {
    
    /**
     * Browser 에러 타입
     */
    sealed class BrowserError : Exception() {
        object PageLoadError : BrowserError()
        object BookmarkAddedSuccess : BrowserError()  // Effect로 이동 예정
        object BookmarkRemovedSuccess : BrowserError() // Effect로 이동 예정
    }
    
    /**
     * Browser 화면의 상태
     */
    data class State(
        val url: String = "https://www.anamandroid.com/",
        val isLoading: Boolean = false,
        val canGoBack: Boolean = false,
        val canGoForward: Boolean = false,
        val bookmarks: List<Bookmark> = emptyList(),
        val isBookmarked: Boolean = false,
        val showUrlBar: Boolean = false,
        val urlInput: String = "",
        val searchSuggestions: List<String> = emptyList(),
        val showBookmarks: Boolean = true, // 초기 화면에 북마크 표시
        val pageTitle: String = "",
        val error: BrowserError? = null,
        val activeBlockchainId: String? = null,
        val isBridgeLoaded: Boolean = false
    )
    
    /**
     * 사용자 액션
     */
    sealed interface Intent {
        data class LoadUrl(val url: String) : Intent
        data object GoBack : Intent
        data object GoForward : Intent
        data object Reload : Intent
        data object ToggleBookmark : Intent
        data object ShowUrlBar : Intent
        data object HideUrlBar : Intent
        data class UpdateUrlInput(val input: String) : Intent
        data object ClearUrlInput : Intent
        data class SelectSuggestion(val suggestion: String) : Intent
        data class SelectBookmark(val bookmark: Bookmark) : Intent
        data class DeleteBookmark(val bookmark: Bookmark) : Intent
        data object ClearError : Intent
        data object ShowBookmarks : Intent
        data class HandleUniversalRequest(val requestId: String, val payload: String) : Intent
        data class LoadBlockchainBridge(val blockchainId: String) : Intent
    }
    
    /**
     * 일회성 이벤트
     */
    sealed interface Effect {
        data object BookmarkAdded : Effect
        data object BookmarkRemoved : Effect
        data object HideKeyboard : Effect
        data class InjectBridgeScript(val script: String) : Effect
        data class SendUniversalResponse(val requestId: String, val response: String) : Effect
    }
}