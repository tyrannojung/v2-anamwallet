package com.anam145.wallet.feature.miniapp.webapp.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.language.getStringsForSkinAndLanguage
import com.anam145.wallet.core.common.model.Skin
import com.anam145.wallet.core.common.model.Language
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import androidx.compose.runtime.CompositionLocalProvider
import com.anam145.wallet.feature.miniapp.webapp.ui.components.WebAppWebView
import com.anam145.wallet.feature.miniapp.common.ui.components.ErrorContent
import com.anam145.wallet.feature.miniapp.common.ui.components.ServiceConnectionCard
import com.anam145.wallet.feature.miniapp.webapp.ui.components.VPBottomSheet
import com.anam145.wallet.feature.miniapp.webapp.ui.components.TransactionApprovalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAppScreen(
    appId: String,
    viewModel: WebAppViewModel,
    fileManager: MiniAppFileManager,
    skin: Skin = Skin.ANAM,
    language: Language = Language.KOREAN
) {
    // 스킨과 언어에 맞는 문자열 가져오기
    val strings = getStringsForSkinAndLanguage(skin, language)
    
    // CompositionLocal로 문자열 제공
    CompositionLocalProvider(
        LocalStrings provides strings
    ) {
        WebAppScreenContent(
            appId = appId,
            viewModel = viewModel,
            fileManager = fileManager
        )
    }
}

@Composable
private fun WebAppScreenContent(
    appId: String,
    viewModel: WebAppViewModel,
    fileManager: MiniAppFileManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    val strings = LocalStrings.current
    
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
                is WebAppContract.Effect.SendVPResponse -> {
                    // VP 응답을 JavaScript로 전달
                    // JSON 문자열을 JavaScript에서 파싱할 수 있도록 escape 처리
                    val escapedJson = effect.vpJson
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                    
                    val script = """
                        (function() {
                            const vpData = JSON.parse("${escapedJson}");
                            const event = new CustomEvent('vpResponse', {
                                detail: vpData
                            });
                            window.dispatchEvent(event);
                        })();
                    """.trimIndent()
                    
                    webView?.evaluateJavascript(script, null)
                }
                is WebAppContract.Effect.SendVPError -> {
                    // VP 에러를 JavaScript로 전달
                    val script = """
                        (function() {
                            const event = new CustomEvent('vpError', {
                                detail: { error: '${effect.error}' }
                            });
                            window.dispatchEvent(event);
                        })();
                    """.trimIndent()
                    
                    webView?.evaluateJavascript(script, null)
                }
                is WebAppContract.Effect.SendTransactionError -> {
                    // 트랜잭션 에러를 JavaScript로 전달
                    val script = """
                        (function() {
                            const event = new CustomEvent('transactionError', {
                                detail: { error: '${effect.error}' }
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
                title = strings.headerTitle,
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
                            onVPRequest = { vpRequest ->
                                viewModel.handleIntent(
                                    WebAppContract.Intent.RequestVP(vpRequest)
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
    
    // VP 바텀시트
    if (uiState.showVPBottomSheet) {
        uiState.vpRequestData?.let { vpRequestData ->
            VPBottomSheet(
                serviceName = vpRequestData.service,
                purpose = vpRequestData.purpose,
                credentialType = vpRequestData.type,
                credentials = uiState.credentials,
                onCredentialSelected = { credentialId ->
                    viewModel.handleIntent(WebAppContract.Intent.SelectCredential(credentialId))
                },
                onDismiss = {
                    viewModel.handleIntent(WebAppContract.Intent.DismissVPBottomSheet)
                }
            )
        }
    }
    
    // 트랜잭션 승인 바텀시트
    if (uiState.showTransactionApproval) {
        uiState.pendingTransactionJson?.let { transactionJson ->
            TransactionApprovalBottomSheet(
                blockchainName = uiState.activeBlockchainName ?: "Blockchain",
                transactionJson = transactionJson,
                onApprove = {
                    viewModel.handleIntent(WebAppContract.Intent.ApproveTransaction)
                },
                onReject = {
                    viewModel.handleIntent(WebAppContract.Intent.RejectTransaction)
                },
                onDismiss = {
                    viewModel.handleIntent(WebAppContract.Intent.DismissTransactionApproval)
                }
            )
        }
    }
}