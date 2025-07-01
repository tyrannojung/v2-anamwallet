package com.anam145.wallet.feature.miniapp.blockchain.ui.components

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.common.webview.WebViewFactory
import java.io.File

@Composable
fun BlockchainWebView(
    blockchainId: String,
    fileManager: MiniAppFileManager,
    onWebViewCreated: (WebView) -> Unit
) {
    val context = LocalContext.current
    
    // JavaScript Bridge 생성
    val bridge = remember { BlockchainUIJavaScriptBridge(context, blockchainId) }
    
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
            
            // WebView 참조를 bridge에 설정
            bridge.setWebView(webView)
            
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
class BlockchainUIJavaScriptBridge(
    private val context: Context,
    private val blockchainId: String
) {
    private var webView: WebView? = null
    
    fun setWebView(webView: WebView) {
        this.webView = webView
    }
    
    @android.webkit.JavascriptInterface
    fun log(message: String) {
        // JavaScript 로그 (필요시에만 활성화)
        // Log.d("BlockchainUI", "JS: $message")
    }
    
    @android.webkit.JavascriptInterface
    fun navigateTo(pagePath: String) {
        Log.d("BlockchainUI", "Navigate to: $pagePath")
        
        (context as? ComponentActivity)?.runOnUiThread {
            webView?.let { web ->
                // 현재 URL에서 도메인 추출
                val domain = "https://$blockchainId.miniapp.local/"
                
                // 쿼리 파라미터 분리
                val parts = pagePath.split("?", limit = 2)
                val path = parts[0]
                val queryString = if (parts.size > 1) "?${parts[1]}" else ""
                
                // 페이지 경로 정리 (확장자 추가)
                val page = if (path.endsWith(".html")) path else "$path.html"
                
                // 전체 URL 생성
                val fullUrl = domain + page + queryString
                
                Log.d("BlockchainUI", "Navigating to: $fullUrl")
                web.loadUrl(fullUrl)
            } ?: Log.e("BlockchainUI", "WebView is null, cannot navigate")
        } ?: Log.e("BlockchainUI", "Context is not ComponentActivity")
    }
}