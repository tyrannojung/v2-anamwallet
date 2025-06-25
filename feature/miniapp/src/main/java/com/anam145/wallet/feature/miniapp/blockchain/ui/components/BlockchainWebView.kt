package com.anam145.wallet.feature.miniapp.blockchain.ui.components

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.miniapp.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.webview.common.WebViewFactory
import java.io.File

@Composable
fun BlockchainWebView(
    blockchainId: String,
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
        // JavaScript 로그 (필요시에만 활성화)
        // Log.d("BlockchainUI", "JS: $message")
    }
    
    @android.webkit.JavascriptInterface
    fun navigateTo(page: String) {
        Log.d("BlockchainUI", "Navigate to: $page")
        // TODO: 페이지 네비게이션 구현
    }
}