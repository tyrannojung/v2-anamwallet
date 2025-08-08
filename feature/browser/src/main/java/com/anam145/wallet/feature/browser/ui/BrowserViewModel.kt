package com.anam145.wallet.feature.browser.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam145.wallet.core.data.datastore.BlockchainDataStore
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.browser.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
    private val checkBookmarkStatusUseCase: CheckBookmarkStatusUseCase,
    private val blockchainDataStore: BlockchainDataStore,
    private val fileManager: MiniAppFileManager,
    private val requestUniversalMethodUseCase: RequestUniversalMethodUseCase,
    private val getInstalledMiniAppsUseCase: com.anam145.wallet.feature.miniapp.common.domain.usecase.GetInstalledMiniAppsUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "BrowserViewModel"
    }
    
    private val _uiState = MutableStateFlow(BrowserContract.State())
    val uiState: StateFlow<BrowserContract.State> = _uiState.asStateFlow()
    
    // 브릿지 스크립트 캐시: blockchainId -> script
    private val bridgeScriptCache = mutableMapOf<String, String>()
    
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
        observeActiveBlockchain()
        
        // 초기 활성 블록체인 확인
        viewModelScope.launch {
            val activeBlockchain = blockchainDataStore.activeBlockchainId.first()
            Log.d(TAG, "Initial active blockchain: $activeBlockchain")
            activeBlockchain?.let {
                handleIntent(BrowserContract.Intent.LoadBlockchainBridge(it))
            }
        }
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
            BrowserContract.Intent.ClearUrlInput -> clearUrlInput()
            is BrowserContract.Intent.SelectSuggestion -> selectSuggestion(intent.suggestion)
            is BrowserContract.Intent.SelectBookmark -> selectBookmark(intent.bookmark)
            is BrowserContract.Intent.DeleteBookmark -> deleteBookmark(intent.bookmark)
            is BrowserContract.Intent.ClearError -> clearError()
            BrowserContract.Intent.ShowBookmarks -> showBookmarks()
            is BrowserContract.Intent.HandleUniversalRequest -> handleUniversalRequest(intent.requestId, intent.payload)
            is BrowserContract.Intent.LoadBlockchainBridge -> loadBlockchainBridge(intent.blockchainId)
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
        // 검색 모드 진입 시 기본 제안 표시
        val defaultSuggestions = if (_uiState.value.url.isNotEmpty() && _uiState.value.url != "about:blank") {
            listOf(
                "Search on DuckDuckGo",
                _uiState.value.url  // 현재 페이지 URL
            )
        } else {
            listOf("Search on DuckDuckGo")
        }
        
        _uiState.update { 
            it.copy(
                showUrlBar = true,
                urlInput = "",  // 빈 입력창으로 시작
                searchSuggestions = defaultSuggestions  // 기본 제안 표시
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
        val suggestions = when {
            // 이미 완전한 URL인 경우
            query.startsWith("http://") || query.startsWith("https://") -> {
                listOf(query)  // URL 그대로 표시
            }
            // 도메인 형식인 경우
            query.contains(".") -> {
                listOf(query)  // 도메인 그대로 표시
            }
            // 일반 검색어인 경우
            else -> {
                listOf("Search \"$query\" on DuckDuckGo")  // DuckDuckGo 검색만
            }
        }
        _uiState.update { it.copy(searchSuggestions = suggestions) }
    }
    
    private fun selectSuggestion(suggestion: String) {
        val url = when {
            // 기본 DuckDuckGo 검색
            suggestion == "Search on DuckDuckGo" -> {
                // 입력값이 있으면 그걸로 검색, 없으면 DuckDuckGo 홈
                val query = _uiState.value.urlInput
                if (query.isNotEmpty()) {
                    "https://duckduckgo.com/?q=$query"
                } else {
                    "https://duckduckgo.com"
                }
            }
            // DuckDuckGo 검색
            suggestion.startsWith("Search ") && suggestion.contains(" on DuckDuckGo") -> {
                val query = suggestion.removePrefix("Search \"").removeSuffix("\" on DuckDuckGo")
                "https://duckduckgo.com/?q=$query"
            }
            // 완전한 URL
            suggestion.startsWith("http://") || suggestion.startsWith("https://") -> {
                suggestion
            }
            // 도메인
            suggestion.contains(".") -> {
                "https://$suggestion"
            }
            else -> suggestion
        }
        loadUrl(url)
    }
    
    private fun clearUrlInput() {
        _uiState.update { it.copy(urlInput = "", searchSuggestions = emptyList()) }
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
    
    /**
     * 활성 블록체인 변경 감지
     */
    private fun observeActiveBlockchain() {
        viewModelScope.launch {
            blockchainDataStore.activeBlockchainId.collect { blockchainId ->
                blockchainId?.let {
                    handleIntent(BrowserContract.Intent.LoadBlockchainBridge(it))
                }
            }
        }
    }
    
    /**
     * 블록체인 Bridge 스크립트 로드
     * 1. 메모리 캐시 확인
     * 2. MiniAppScanner 캐시에서 bridge 정보 확인
     * 3. 없으면 파일에서 직접 로드 (fallback)
     */
    private fun loadBlockchainBridge(blockchainId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isBridgeLoaded = false) }
                
                // 1. 메모리 캐시 확인
                bridgeScriptCache[blockchainId]?.let { cachedScript ->
                    Log.d(TAG, "Using cached bridge script for: $blockchainId")
                    _effect.emit(BrowserContract.Effect.InjectBridgeScript(cachedScript))
                    _uiState.update { 
                        it.copy(
                            activeBlockchainId = blockchainId,
                            isBridgeLoaded = true
                        )
                    }
                    return@launch
                }
                
                // 2. MiniAppScanner 캐시에서 bridge 정보 확인
                val miniAppsResult = withContext(Dispatchers.IO) {
                    getInstalledMiniAppsUseCase()
                }
                
                when (miniAppsResult) {
                    is com.anam145.wallet.core.common.result.MiniAppResult.Success -> {
                        val miniApp = miniAppsResult.data[blockchainId]
                        
                        miniApp?.bridge?.let { bridgeConfig ->
                            Log.d(TAG, "Bridge config found in cache for $blockchainId: ${bridgeConfig.script}")
                            
                            // Bridge 스크립트 로드
                            val scriptResult = withContext(Dispatchers.IO) {
                                fileManager.loadBridgeScript(blockchainId, bridgeConfig.script)
                            }
                            
                            when (scriptResult) {
                                is com.anam145.wallet.core.common.result.MiniAppResult.Success -> {
                                    val bridgeScript = scriptResult.data
                                    
                                    // 메모리 캐시에 저장
                                    bridgeScriptCache[blockchainId] = bridgeScript
                                    
                                    // Bridge 스크립트 주입
                                    _effect.emit(
                                        BrowserContract.Effect.InjectBridgeScript(bridgeScript)
                                    )
                                    
                                    _uiState.update { 
                                        it.copy(
                                            activeBlockchainId = blockchainId,
                                            isBridgeLoaded = true
                                        )
                                    }
                                    
                                    Log.d(TAG, "Bridge loaded and cached for: $blockchainId")
                                }
                                is com.anam145.wallet.core.common.result.MiniAppResult.Error -> {
                                    Log.e(TAG, "Failed to load bridge script: $scriptResult")
                                    updateNoBridge(blockchainId)
                                }
                            }
                        } ?: run {
                            // Bridge 설정이 없는 경우 - 정상적인 상황
                            Log.d(TAG, "No bridge configuration for: $blockchainId")
                            updateNoBridge(blockchainId)
                        }
                    }
                    is com.anam145.wallet.core.common.result.MiniAppResult.Error -> {
                        // 3. Fallback: 캐시가 없으면 직접 manifest 로드
                        Log.d(TAG, "MiniApp cache miss, loading manifest directly for: $blockchainId")
                        loadBridgeFromManifest(blockchainId)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load bridge", e)
                updateNoBridge(blockchainId)
            }
        }
    }
    
    /**
     * Fallback: manifest에서 직접 bridge 로드
     */
    private suspend fun loadBridgeFromManifest(blockchainId: String) {
        val manifestResult = withContext(Dispatchers.IO) {
            fileManager.loadManifest(blockchainId)
        }
        
        when (manifestResult) {
            is com.anam145.wallet.core.common.result.MiniAppResult.Success -> {
                val manifest = manifestResult.data
                
                manifest.bridge?.let { bridgeConfig ->
                    Log.d(TAG, "Bridge script found in manifest for $blockchainId: ${bridgeConfig.script}")
                    
                    val scriptResult = withContext(Dispatchers.IO) {
                        fileManager.loadBridgeScript(blockchainId, bridgeConfig.script)
                    }
                    
                    when (scriptResult) {
                        is com.anam145.wallet.core.common.result.MiniAppResult.Success -> {
                            val bridgeScript = scriptResult.data
                            
                            // 메모리 캐시에 저장
                            bridgeScriptCache[blockchainId] = bridgeScript
                            
                            _effect.emit(
                                BrowserContract.Effect.InjectBridgeScript(bridgeScript)
                            )
                            
                            _uiState.update { 
                                it.copy(
                                    activeBlockchainId = blockchainId,
                                    isBridgeLoaded = true
                                )
                            }
                            
                            Log.d(TAG, "Bridge loaded from manifest for: $blockchainId")
                        }
                        is com.anam145.wallet.core.common.result.MiniAppResult.Error -> {
                            Log.e(TAG, "Failed to load bridge script: $scriptResult")
                            updateNoBridge(blockchainId)
                        }
                    }
                } ?: updateNoBridge(blockchainId)
            }
            is com.anam145.wallet.core.common.result.MiniAppResult.Error -> {
                Log.e(TAG, "Failed to load manifest: $manifestResult")
                updateNoBridge(blockchainId)
            }
        }
    }
    
    private fun updateNoBridge(blockchainId: String) {
        _uiState.update { 
            it.copy(
                activeBlockchainId = blockchainId,
                isBridgeLoaded = false
            )
        }
    }
    
    /**
     * Universal Bridge 요청 처리
     */
    private fun handleUniversalRequest(requestId: String, payload: String) {
        viewModelScope.launch {
            try {
                val activeBlockchain = _uiState.value.activeBlockchainId
                    ?: throw IllegalStateException("No active blockchain")
                
                // UseCase를 통해 요청 전달
                val response = requestUniversalMethodUseCase(
                    requestId = requestId,
                    blockchainId = activeBlockchain,
                    payload = payload
                )
                
                // 응답 전송
                _effect.emit(
                    BrowserContract.Effect.SendUniversalResponse(
                        requestId = requestId,
                        response = response
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Universal request failed", e)
                
                // 에러 응답
                _effect.emit(
                    BrowserContract.Effect.SendUniversalResponse(
                        requestId = requestId,
                        response = """{"error": "${e.message}"}"""
                    )
                )
            }
        }
    }
}