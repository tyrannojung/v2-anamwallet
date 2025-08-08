package com.anam145.wallet.feature.browser.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.theme.ShapeCard
import com.anam145.wallet.feature.browser.domain.model.Bookmark
import com.anam145.wallet.feature.browser.ui.BrowserContract
// v2.0: getUniversalBridgeScript import 제거됨 (더 이상 Native에서 주입하지 않음)
import com.anam145.wallet.feature.browser.ui.BrowserViewModel
import com.anam145.wallet.feature.browser.ui.components.*
import kotlinx.coroutines.launch
import android.widget.Toast
import android.util.Log

/**
 * Browser 에러를 문자열로 변환
 */
@Composable
private fun BrowserContract.BrowserError.toMessage(): String {
    val strings = LocalStrings.current
    return when (this) {
        BrowserContract.BrowserError.PageLoadError -> strings.browserPageLoadError
        BrowserContract.BrowserError.BookmarkAddedSuccess -> strings.browserBookmarkAdded
        BrowserContract.BrowserError.BookmarkRemovedSuccess -> strings.browserBookmarkRemoved
    }
}

/**
 * 브라우저 화면
 * 
 * MetaMask 스타일의 웹 브라우저
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BrowserScreen(
    modifier: Modifier = Modifier,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val strings = LocalStrings.current
    
    // WebView 참조
    var webView by remember { mutableStateOf<WebView?>(null) }
    // Bridge Script 상태
    var bridgeScript by remember { mutableStateOf<String?>(null) }
    
    // Effects 처리
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BrowserContract.Effect.BookmarkAdded -> {
                    Toast.makeText(context, strings.browserBookmarkAdded, Toast.LENGTH_SHORT).show()
                }
                is BrowserContract.Effect.BookmarkRemoved -> {
                    Toast.makeText(context, strings.browserBookmarkRemoved, Toast.LENGTH_SHORT).show()
                }
                is BrowserContract.Effect.HideKeyboard -> {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
                is BrowserContract.Effect.InjectBridgeScript -> {
                    val wasNull = bridgeScript == null
                    bridgeScript = effect.script
                    
                    // WebView가 이미 로드되어 있으면
                    webView?.let { view ->
                        val currentUrl = view.url
                        
                        // 첫 번째 bridge 로드이고 이미 페이지가 로드된 경우 새로고침
                        if (wasNull && currentUrl != null && currentUrl != "about:blank") {
                            Log.d("BrowserScreen", "Bridge loaded late, reloading page: $currentUrl")
                            view.reload()
                        } else {
                            // v2.0: blockchain bridge만 주입 (WalletBridge 포함)
                            view.evaluateJavascript(effect.script, null)
                        }
                    }
                }
                is BrowserContract.Effect.SendUniversalResponse -> {
                    val bridge = webView?.tag as? com.anam145.wallet.feature.browser.bridge.BrowserJavaScriptBridge
                    bridge?.sendUniversalResponse(effect.requestId, effect.response)
                }
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // URL 바 또는 간단한 상태 바
            BrowserTopBar(
                url = uiState.url,
                pageTitle = uiState.pageTitle,
                isLoading = uiState.isLoading,
                isBookmarked = uiState.isBookmarked,
                showUrlBar = uiState.showUrlBar,
                urlInput = uiState.urlInput,
                searchSuggestions = uiState.searchSuggestions,
                onUrlInputChange = { viewModel.handleIntent(BrowserContract.Intent.UpdateUrlInput(it)) },
                onUrlSubmit = { viewModel.handleIntent(BrowserContract.Intent.LoadUrl(it)) },
                onBookmarkClick = { viewModel.handleIntent(BrowserContract.Intent.ToggleBookmark) },
                onShowUrlBar = { viewModel.handleIntent(BrowserContract.Intent.ShowUrlBar) },
                onHideUrlBar = { viewModel.handleIntent(BrowserContract.Intent.HideUrlBar) },
                onSuggestionClick = { viewModel.handleIntent(BrowserContract.Intent.SelectSuggestion(it)) }
            )
            
            // WebView 또는 북마크 화면
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.showBookmarks && !uiState.showUrlBar) {
                    // 북마크 화면
                    BookmarksView(
                        bookmarks = uiState.bookmarks,
                        onBookmarkClick = { viewModel.handleIntent(BrowserContract.Intent.SelectBookmark(it)) },
                        onBookmarkDelete = { viewModel.handleIntent(BrowserContract.Intent.DeleteBookmark(it)) }
                    )
                } else {
                    // WebView
                    BrowserWebView(
                        url = uiState.url,
                        onWebViewCreated = { webView = it },
                        onPageStarted = { url ->
                            viewModel.updateWebViewState(
                                canGoBack = webView?.canGoBack() ?: false,
                                canGoForward = webView?.canGoForward() ?: false,
                                url = url,
                                title = "",
                                isLoading = true
                            )
                        },
                        onPageFinished = { url ->
                            viewModel.updateWebViewState(
                                canGoBack = webView?.canGoBack() ?: false,
                                canGoForward = webView?.canGoForward() ?: false,
                                url = url,
                                title = webView?.title ?: "",
                                isLoading = false
                            )
                        },
                        onPageError = { error ->
                            viewModel.onPageError(error)
                        },
                        onUniversalRequest = { requestId, payload ->
                            viewModel.handleIntent(
                                BrowserContract.Intent.HandleUniversalRequest(
                                    requestId = requestId,
                                    payload = payload
                                )
                            )
                        },
                        bridgeScript = bridgeScript
                    )
                    
                    // 에러 화면
                    uiState.error?.let { error ->
                        ErrorOverlay(
                            error = error.toMessage(),
                            onDismiss = { viewModel.handleIntent(BrowserContract.Intent.ClearError) }
                        )
                    }
                }
            }
            
            // 하단 네비게이션 바
            BrowserBottomBar(
                canGoBack = uiState.canGoBack,
                canGoForward = uiState.canGoForward,
                onBackClick = { webView?.goBack() },
                onForwardClick = { webView?.goForward() },
                onReloadClick = { 
                    webView?.reload()
                    viewModel.handleIntent(BrowserContract.Intent.Reload)
                },
                onHomeClick = {
                    // 홈 버튼을 누르면 북마크 페이지로 이동
                    viewModel.handleIntent(BrowserContract.Intent.ShowBookmarks)
                }
            )
        }
    }
}