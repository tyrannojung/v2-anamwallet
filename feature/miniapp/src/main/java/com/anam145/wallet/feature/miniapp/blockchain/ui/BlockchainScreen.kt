package com.anam145.wallet.feature.miniapp.blockchain.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.blockchain.ui.components.BlockchainWebView
import com.anam145.wallet.feature.miniapp.blockchain.ui.components.ErrorContent
import com.anam145.wallet.feature.miniapp.blockchain.ui.components.ServiceConnectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockchainScreen(
    blockchainId: String,
    viewModel: BlockchainViewModel,
    fileManager: MiniAppFileManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // 초기화
    LaunchedEffect(key1 = blockchainId) {
        viewModel.initialize(blockchainId)
    }
    
    // URL 로드 처리
    LaunchedEffect(key1 = uiState.webUrl) {
        uiState.webUrl?.let { url ->
            webView?.loadUrl(url)
        }
    }
    
    Scaffold(
        topBar = {
            Header(
                title = "AnamWallet",
                showBackButton = true,
                onBackClick = {
                    viewModel.handleIntent(BlockchainContract.Intent.NavigateBack)
                },
                showBlockchainStatus = uiState.isActivated,
                activeBlockchainName = if (uiState.isActivated) {
                    uiState.manifest?.name ?: "Activated"
                } else null
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
                            viewModel.handleIntent(BlockchainContract.Intent.DismissError)
                            viewModel.initialize(blockchainId)
                        }
                    )
                }
                uiState.manifest != null -> {
                    uiState.manifest?.let { manifest ->
                        BlockchainWebView(
                            blockchainId = blockchainId,
                            manifest = manifest,
                            fileManager = fileManager,
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
                        viewModel.handleIntent(BlockchainContract.Intent.RetryServiceConnection)
                    }
                )
            }
        }
    }
}