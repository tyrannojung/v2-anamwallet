package com.anam145.wallet.feature.miniapp.blockchain.ui.components

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.common.webview.WebViewFactory
import com.anam145.wallet.feature.miniapp.blockchain.bridge.BlockchainUIJavaScriptBridge
import java.io.File

@Composable
fun BlockchainWebView(
    blockchainId: String,
    manifest: MiniAppManifest,
    fileManager: MiniAppFileManager,
    onWebViewCreated: (WebView) -> Unit
) {
    val context = LocalContext.current
    
    // JavaScript Bridge 생성
    val bridge = remember { BlockchainUIJavaScriptBridge(context, blockchainId, manifest) }
    
    // Bridge 정리를 위한 DisposableEffect
    DisposableEffect(bridge) {
        onDispose {
            // Composable이 dispose될 때 bridge 정리
            bridge.destroy()
        }
    }
    
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