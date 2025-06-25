package com.anam145.wallet.feature.miniapp.webapp.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.webapp.ui.components.WebAppWebView
import com.anam145.wallet.feature.miniapp.webapp.ui.components.ErrorContent
import com.anam145.wallet.feature.miniapp.webapp.ui.components.ServiceConnectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAppScreen(
    appId: String,
    viewModel: WebAppViewModel,
    fileManager: MiniAppFileManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // 초기화
    LaunchedEffect(key1 = appId) {
        viewModel.initialize(appId)
    }

    // URL 로드 처리
    LaunchedEffect(key1 = uiState.webUrl) {
        uiState.webUrl?.let { url ->
            webView?.loadUrl(url)
        }
    }
    
    // Effect 처리
    LaunchedEffect(key1 = viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WebAppContract.Effect.SendTransactionResponse -> {
                    // WebView에 JavaScript 실행
                    val script = """
                        (function() {
                            const event = new CustomEvent('transactionResponse', {
                                detail: ${effect.responseJson}
                            });
                            window.dispatchEvent(event);
                        })();
                    """.trimIndent()
                    
                    webView?.evaluateJavascript(script, null)
                }
                else -> {
                    // Activity에서 처리
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            Header(
                title = "AnamWallet",
                showBackButton = true,
                onBackClick = {
                    viewModel.handleIntent(WebAppContract.Intent.NavigateBack)
                },
                showBlockchainStatus = !uiState.activeBlockchainName.isNullOrEmpty(),
                activeBlockchainName = uiState.activeBlockchainName
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error ?: "오류가 발생했습니다",
                        onRetry = {
                            viewModel.handleIntent(WebAppContract.Intent.DismissError)
                            viewModel.initialize(appId)
                        }
                    )
                }
                uiState.manifest != null -> {
                    uiState.manifest?.let { manifest ->
                        WebAppWebView(
                            appId = appId,
                            manifest = manifest,
                            fileManager = fileManager,
                            onTransactionRequest = { transactionData ->
                                viewModel.handleIntent(
                                    WebAppContract.Intent.RequestTransaction(transactionData)
                                )
                            },
                            onWebViewCreated = { 
                                webView = it
                                viewModel.onWebViewReady()
                            }
                        )
                    }
                }
            }
            
            // 서비스 연결 상태 표시
            if (!uiState.isServiceConnected && !uiState.isLoading) {
                ServiceConnectionCard(
                    onRetry = {
                        viewModel.handleIntent(WebAppContract.Intent.RetryServiceConnection)
                    }
                )
            }
        }
    }
}