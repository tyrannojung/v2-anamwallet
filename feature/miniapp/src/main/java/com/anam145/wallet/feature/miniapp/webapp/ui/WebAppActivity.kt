package com.anam145.wallet.feature.miniapp.webapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.feature.miniapp.webapp.bridge.WebAppJavaScriptBridge
import com.anam145.wallet.feature.miniapp.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.webview.common.WebViewFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

/**
 * 일반 WebApp을 표시하는 Activity
 * 
 * :app 프로세스에서 실행되며, WebAppService를 통해
 * 블록체인 서비스와 통신합니다.
 * MVI 패턴을 사용하여 상태 관리를 합니다.
 */
@AndroidEntryPoint
class WebAppActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "WebAppActivity"
        const val EXTRA_APP_ID = "app_id"
        
        fun createIntent(context: Context, appId: String): Intent {
            return Intent(context, WebAppActivity::class.java).apply {
                putExtra(EXTRA_APP_ID, appId)
            }
        }
    }
    
    @Inject
    lateinit var fileManager: MiniAppFileManager
    
    private val viewModel: WebAppViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appId = intent.getStringExtra(EXTRA_APP_ID) ?: ""
        
        // Effect 처리
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect
                    .onEach { effect ->
                        when (effect) {
                            is WebAppContract.Effect.NavigateBack -> {
                                finish()
                            }
                            is WebAppContract.Effect.ShowError -> {
                                runOnUiThread {
                                    Toast.makeText(this@WebAppActivity, effect.message, Toast.LENGTH_LONG).show()
                                    Log.d(TAG, "Showing toast: ${effect.message}")
                                }
                            }
                            is WebAppContract.Effect.SendPaymentResponse,
                            is WebAppContract.Effect.LoadUrl -> {
                                // WebView에서 처리
                            }
                        }
                    }
                    .launchIn(this)
            }
        }
        
        setContent {
            AnamWalletTheme {
                WebAppScreen(
                    appId = appId,
                    viewModel = viewModel,
                    fileManager = fileManager
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // ViewModel이 서비스 정리를 담당
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAppScreen(
    appId: String,
    viewModel: WebAppViewModel,
    fileManager: MiniAppFileManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
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
            // 통합된 헤더 사용
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

@Composable
fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge
        )
        TextButton(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}

@Composable
fun ServiceConnectionCard(
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "서비스 연결 끊김",
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onRetry) {
                Text("재연결")
            }
        }
    }
}

@Composable
fun WebAppWebView(
    appId: String,
    manifest: com.anam145.wallet.core.common.model.MiniAppManifest,
    fileManager: MiniAppFileManager,
    onPaymentRequest: (JSONObject) -> Unit,
    onWebViewCreated: (WebView) -> Unit
) {
    val context = LocalContext.current
    
    // JavaScript Bridge 생성
    val jsBridge = remember(manifest) {
        WebAppJavaScriptBridge(
            context = context,
            manifest = manifest,
            onPaymentRequest = onPaymentRequest
        )
    }
    
    AndroidView(
        factory = { ctx ->
            // WebViewFactory를 사용하여 WebView 생성
            val webView = WebViewFactory.create(
                context = ctx,
                appId = appId,
                baseDir = File(fileManager.getMiniAppBasePath(appId)),
                headless = false,
                jsBridge = jsBridge,
                enableDebugging = false
            )
            
            // Bridge에 WebView 설정
            jsBridge.webView = webView
            
            onWebViewCreated(webView)
            webView
        },
        modifier = Modifier.fillMaxSize()
    )
}