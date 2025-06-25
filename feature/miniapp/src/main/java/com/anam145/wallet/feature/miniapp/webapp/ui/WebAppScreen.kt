package com.anam145.wallet.feature.miniapp.webapp.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.feature.miniapp.data.common.MiniAppFileManager
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
    
    // 초기 로드
    LaunchedEffect(appId) {
        viewModel.processIntent(WebAppContract.Intent.LoadWebApp(appId))
    }
    
    // Effect 처리
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WebAppContract.Effect.SendPaymentResponse -> {
                    // WebView에 JavaScript 실행
                    val script = """
                        (function() {
                            const event = new CustomEvent('paymentResponse', {
                                detail: ${effect.responseJson}
                            });
                            window.dispatchEvent(event);
                        })();
                    """.trimIndent()
                    
                    webView?.evaluateJavascript(script, null)
                }
                is WebAppContract.Effect.LoadUrl -> {
                    webView?.loadUrl(effect.url)
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
                    viewModel.processIntent(WebAppContract.Intent.NavigateBack)
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
                            viewModel.processIntent(WebAppContract.Intent.DismissError)
                            viewModel.processIntent(WebAppContract.Intent.LoadWebApp(appId))
                        }
                    )
                }
                uiState.manifest != null -> {
                    uiState.manifest?.let { manifest ->
                        WebAppWebView(
                            appId = appId,
                            manifest = manifest,
                            fileManager = fileManager,
                            onPaymentRequest = { paymentData ->
                                viewModel.processIntent(
                                    WebAppContract.Intent.RequestPayment(paymentData)
                                )
                            },
                            onWebViewCreated = { 
                                webView = it
                                viewModel.processIntent(WebAppContract.Intent.WebViewReady)
                            }
                        )
                    }
                }
            }
            
            // 서비스 연결 상태 표시
            if (!uiState.isServiceConnected && !uiState.isLoading) {
                ServiceConnectionCard(
                    onRetry = {
                        viewModel.processIntent(WebAppContract.Intent.RetryServiceConnection)
                    }
                )
            }
        }
    }
}