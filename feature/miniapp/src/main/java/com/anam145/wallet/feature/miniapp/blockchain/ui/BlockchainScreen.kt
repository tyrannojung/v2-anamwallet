package com.anam145.wallet.feature.miniapp.blockchain.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.feature.miniapp.data.common.MiniAppFileManager
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
    
    // 초기 로드
    LaunchedEffect(blockchainId) {
        viewModel.processIntent(BlockchainContract.Intent.LoadBlockchain(blockchainId))
    }
    
    // Effect 처리
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BlockchainContract.Effect.LoadUrl -> {
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
                    viewModel.processIntent(BlockchainContract.Intent.NavigateBack)
                },
                showBlockchainStatus = uiState.isActivated,
                activeBlockchainName = if (uiState.isActivated) {
                    uiState.manifest?.name ?: "활성화됨"
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
                            viewModel.processIntent(BlockchainContract.Intent.DismissError)
                            viewModel.processIntent(BlockchainContract.Intent.LoadBlockchain(blockchainId))
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
                                viewModel.processIntent(BlockchainContract.Intent.WebViewReady)
                            }
                        )
                    }
                }
            }
            
            // 서비스 연결 상태 표시
            if (!uiState.isServiceConnected && !uiState.isLoading) {
                ServiceConnectionCard(
                    onRetry = {
                        viewModel.processIntent(BlockchainContract.Intent.RetryServiceConnection)
                    }
                )
            }
        }
    }
}