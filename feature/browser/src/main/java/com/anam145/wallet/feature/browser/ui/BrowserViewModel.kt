package com.anam145.wallet.feature.browser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.feature.browser.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Browser 화면의 ViewModel
 * 
 * MVI 패턴을 사용하여 브라우저 화면의 상태를 관리합니다.
 * 
 * UseCase 설명:
 * - GetBookmarksUseCase: 저장된 북마크 목록을 가져옴
 * - AddBookmarkUseCase: 새로운 북마크 추가
 * - DeleteBookmarkUseCase: 북마크 삭제
 * - ToggleBookmarkUseCase: 북마크 추가/삭제 토글
 * - CheckBookmarkStatusUseCase: 현재 URL의 북마크 상태 확인
 */
@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val checkBookmarkStatusUseCase: CheckBookmarkStatusUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BrowserContract.State())
    val uiState: StateFlow<BrowserContract.State> = _uiState.asStateFlow()
    
    private val _effect = MutableSharedFlow<BrowserContract.Effect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<BrowserContract.Effect> = _effect.asSharedFlow()
    
    // 현재 URL을 추적하여 북마크 상태를 확인
    private val _currentUrl = MutableStateFlow("https://www.anamandroid.com/")
    
    init {
        observeBookmarks()
        observeCurrentUrl()
    }
    
    fun handleIntent(intent: BrowserContract.Intent) {
        when (intent) {
            is BrowserContract.Intent.LoadUrl -> loadUrl(intent.url)
            is BrowserContract.Intent.GoBack -> {} // WebView에서 직접 처리
            is BrowserContract.Intent.GoForward -> {} // WebView에서 직접 처리
            is BrowserContract.Intent.Reload -> reload()
            is BrowserContract.Intent.ToggleBookmark -> toggleBookmark()
            is BrowserContract.Intent.ShowUrlBar -> showUrlBar()
            is BrowserContract.Intent.HideUrlBar -> hideUrlBar()
            is BrowserContract.Intent.UpdateUrlInput -> updateUrlInput(intent.input)
            is BrowserContract.Intent.SelectSuggestion -> selectSuggestion(intent.suggestion)
            is BrowserContract.Intent.SelectBookmark -> selectBookmark(intent.bookmark)
            is BrowserContract.Intent.DeleteBookmark -> deleteBookmark(intent.bookmark)
            is BrowserContract.Intent.ClearError -> clearError()
            BrowserContract.Intent.ShowBookmarks -> showBookmarks()
        }
    }
    
    private fun observeBookmarks() {
        viewModelScope.launch {
            getBookmarksUseCase().collect { bookmarks ->
                _uiState.update { it.copy(bookmarks = bookmarks) }
            }
        }
    }
    
    private fun observeCurrentUrl() {
        viewModelScope.launch {
            _currentUrl
                .flatMapLatest { url ->
                    // URL이 변경될 때마다 북마크 상태 확인
                    checkBookmarkStatusUseCase(url)
                }
                .collect { isBookmarked ->
                    _uiState.update { it.copy(isBookmarked = isBookmarked) }
                }
        }
    }
    
    private fun loadUrl(url: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    url = url,
                    isLoading = true,
                    showUrlBar = false,
                    showBookmarks = false,
                    error = null
                )
            }
            _currentUrl.value = url
            _effect.emit(BrowserContract.Effect.HideKeyboard)
        }
    }
    
    
    private fun reload() {
        _uiState.update { it.copy(isLoading = true) }
    }
    
    private fun toggleBookmark() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val title = currentState.pageTitle.ifEmpty { currentState.url }
            val url = currentState.url
            
            toggleBookmarkUseCase(title, url)
            
            if (currentState.isBookmarked) {
                _effect.emit(BrowserContract.Effect.BookmarkRemoved)
            } else {
                _effect.emit(BrowserContract.Effect.BookmarkAdded)
            }
        }
    }
    
    private fun showUrlBar() {
        _uiState.update { 
            it.copy(
                showUrlBar = true,
                urlInput = it.url
            )
        }
    }
    
    private fun hideUrlBar() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    showUrlBar = false,
                    urlInput = "",
                    searchSuggestions = emptyList()
                )
            }
            _effect.emit(BrowserContract.Effect.HideKeyboard)
        }
    }
    
    private fun updateUrlInput(input: String) {
        _uiState.update { it.copy(urlInput = input) }
        
        // 검색 추천 업데이트 (추후 DuckDuckGo API 연동)
        if (input.isNotEmpty()) {
            updateSearchSuggestions(input)
        } else {
            _uiState.update { it.copy(searchSuggestions = emptyList()) }
        }
    }
    
    private fun updateSearchSuggestions(query: String) {
        // 임시 추천 목록 (추후 DuckDuckGo API로 대체)
        val suggestions = listOf(
            "https://duckduckgo.com/?q=$query",
            query // 직접 URL 입력
        )
        _uiState.update { it.copy(searchSuggestions = suggestions) }
    }
    
    private fun selectSuggestion(suggestion: String) {
        val url = if (suggestion.startsWith("http://") || suggestion.startsWith("https://")) {
            suggestion
        } else {
            "https://duckduckgo.com/?q=$suggestion"
        }
        loadUrl(url)
    }
    
    private fun selectBookmark(bookmark: com.anam145.wallet.feature.browser.domain.model.Bookmark) {
        loadUrl(bookmark.url)
    }
    
    private fun deleteBookmark(bookmark: com.anam145.wallet.feature.browser.domain.model.Bookmark) {
        viewModelScope.launch {
            deleteBookmarkUseCase(bookmark.id)
            _effect.emit(BrowserContract.Effect.BookmarkRemoved)
        }
    }
    
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun showBookmarks() {
        _uiState.update { 
            it.copy(
                showBookmarks = true,
                showUrlBar = false,
                isLoading = false,
                isBookmarked = false,  // 북마크 페이지에서는 북마크 아이콘 비활성화
                error = null
            )
        }
    }
    
    // WebView에서 호출할 메서드들
    fun updateWebViewState(
        canGoBack: Boolean,
        canGoForward: Boolean,
        url: String,
        title: String,
        isLoading: Boolean
    ) {
        _currentUrl.value = url
        
        _uiState.update { 
            it.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                url = url,
                pageTitle = title,
                isLoading = isLoading
            )
        }
    }
    
    fun onPageError(error: String) {
        _uiState.update { 
            it.copy(
                isLoading = false,
                error = BrowserContract.BrowserError.PageLoadError
            )
        }
    }
}