package com.anam145.wallet.feature.miniapp.blockchain.ui

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
import android.webkit.WebViewClient
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.anam145.wallet.core.ui.theme.AnamWalletTheme
import com.anam145.wallet.core.ui.language.LocalStrings
import com.anam145.wallet.core.ui.components.Header
import com.anam145.wallet.feature.miniapp.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.webview.common.WebViewFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 블록체인 UI를 표시하는 Activity
 * 
 * 블록체인 프로세스(:blockchain)에서 실행되며,
 * BlockchainService와 통신하여 블록체인 상태를 동기화합니다.
 * MVI 패턴을 사용하여 상태 관리를 합니다.
 */
@AndroidEntryPoint
class BlockchainUIActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "BlockchainUIActivity"
        const val EXTRA_BLOCKCHAIN_ID = "blockchain_id"
        
        fun createIntent(context: Context, blockchainId: String): Intent {
            return Intent(context, BlockchainUIActivity::class.java).apply {
                putExtra(EXTRA_BLOCKCHAIN_ID, blockchainId)
                // Ensure the activity is brought to the front
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
    
    // Hilt injection 테스트
    @Inject
    lateinit var testString: String
    
    // TODO: Hilt가 정상 작동하면 @Inject로 변경
    private lateinit var fileManager: MiniAppFileManager
    
    private val viewModel: BlockchainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate - blockchainId: ${intent.getStringExtra(EXTRA_BLOCKCHAIN_ID)}")
        
        // Hilt injection 테스트
        try {
            Log.d(TAG, "Hilt injection test: $testString")
            // Hilt가 작동하면 fileManager도 injection으로 변경 예정
        } catch (e: UninitializedPropertyAccessException) {
            Log.e(TAG, "Hilt injection failed in blockchain process", e)
        }
        
        // 현재는 수동으로 생성 (Hilt 테스트 후 변경 예정)
        fileManager = MiniAppFileManager(this)
        
        val blockchainId = intent.getStringExtra(EXTRA_BLOCKCHAIN_ID) ?: ""
        
        // Effect 처리
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect
                    .onEach { effect ->
                        when (effect) {
                            is BlockchainContract.Effect.NavigateBack -> {
                                Log.d(TAG, "onBack - finishing activity")
                                finish()
                            }
                            is BlockchainContract.Effect.ShowError -> {
                                Toast.makeText(this@BlockchainUIActivity, effect.message, Toast.LENGTH_SHORT).show()
                            }
                            is BlockchainContract.Effect.LoadUrl -> {
                                // WebView에서 처리
                            }
                        }
                    }
                    .launchIn(this)
            }
        }
        
        setContent {
            AnamWalletTheme {
                BlockchainScreen(
                    blockchainId = blockchainId,
                    viewModel = viewModel,
                    fileManager = fileManager
                )
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // ViewModel이 서비스 정리를 담당
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockchainScreen(
    blockchainId: String,
    viewModel: BlockchainViewModel,
    fileManager: MiniAppFileManager
) {
    Log.d("BlockchainScreen", "BlockchainScreen Composable started for: $blockchainId")
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    Log.d("BlockchainScreen", "uiState: isLoading=${uiState.isLoading}, manifest=${uiState.manifest}, error=${uiState.error}")
    
    // 초기 로드
    LaunchedEffect(blockchainId) {
        Log.d("BlockchainScreen", "LaunchedEffect: Calling processIntent with LoadBlockchain")
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
    
    val strings = LocalStrings.current
    
    Scaffold(
        topBar = {
            // 통합된 헤더 사용
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
                    Log.d("BlockchainScreen", "Showing loading indicator")
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Log.d("BlockchainScreen", "Showing error: ${uiState.error}")
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
fun BlockchainWebView(
    blockchainId: String,
    manifest: com.anam145.wallet.core.common.model.MiniAppManifest,
    fileManager: MiniAppFileManager,
    onWebViewCreated: (WebView) -> Unit
) {
    val context = LocalContext.current
    
    // JavaScript Bridge 생성
    val bridge = remember { BlockchainUIJavaScriptBridge(context) }
    
    AndroidView(
        factory = { ctx ->
            // WebViewFactory를 사용하여 WebView 생성
            val webView = WebViewFactory.create(
                context = ctx,
                appId = blockchainId,
                baseDir = File(fileManager.getMiniAppBasePath(blockchainId)),
                headless = false,
                jsBridge = bridge,
                enableDebugging = false
            )
            
            // UI 전용 JavaScript Bridge 추가 (anamUI 네임스페이스)
            @Suppress("JavascriptInterface")
            webView.addJavascriptInterface(bridge, "anamUI")
            
            // WebViewFactory에서 이미 설정한 WebViewClient를 가져와서 확장
            @Suppress("NewApi")
            val originalClient = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                webView.webViewClient
            } else {
                null
            }
            
            // 로깅을 위한 WebViewClient 래퍼
            webView.webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: android.webkit.WebResourceRequest
                ): android.webkit.WebResourceResponse? {
                    Log.d("BlockchainWebView", "Intercepting request: ${request.url}")
                    // 원래 client(AssetLoader 포함)의 메서드 호출
                    return originalClient?.shouldInterceptRequest(view, request)
                }
                
                @Suppress("DEPRECATION")
                override fun shouldInterceptRequest(
                    view: WebView,
                    url: String
                ): android.webkit.WebResourceResponse? {
                    return originalClient?.shouldInterceptRequest(view, url)
                }
                
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("BlockchainWebView", "Page started loading: $url")
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("BlockchainWebView", "Page finished loading: $url")
                    
                    // Check if content is visible
                    view?.evaluateJavascript("document.body.innerHTML.length") { length ->
                        Log.d("BlockchainWebView", "Page content length: $length")
                    }
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e("BlockchainWebView", "Error loading page: ${error?.description}")
                }
            }
            
            onWebViewCreated(webView)
            webView
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 블록체인 UI용 JavaScript Bridge
 */
class BlockchainUIJavaScriptBridge(private val context: Context) {
    
    @android.webkit.JavascriptInterface
    fun log(message: String) {
        Log.d("BlockchainUI", "JS: $message")
    }
    
    @android.webkit.JavascriptInterface
    fun navigateTo(page: String) {
        Log.d("BlockchainUI", "Navigate to: $page")
        // TODO: 페이지 네비게이션 구현
    }
}