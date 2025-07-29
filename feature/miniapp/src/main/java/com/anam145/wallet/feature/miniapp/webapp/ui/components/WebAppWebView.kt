package com.anam145.wallet.feature.miniapp.webapp.ui.components

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.webapp.bridge.WebAppJavaScriptBridge
import com.anam145.wallet.feature.miniapp.common.webview.WebViewFactory
import org.json.JSONObject
import java.io.File

@Composable
fun WebAppWebView(
    appId: String,
    manifest: MiniAppManifest,
    fileManager: MiniAppFileManager,
    onTransactionRequest: (JSONObject) -> Unit,
    onVPRequest: (JSONObject) -> Unit,
    onWebViewCreated: (WebView) -> Unit
) {
    val context = LocalContext.current
    
    // JavaScript Bridge 생성
    val jsBridge = remember(manifest) {
        WebAppJavaScriptBridge(
            context = context,
            manifest = manifest,
            onTransactionRequest = onTransactionRequest,
            onVPRequest = onVPRequest
        )
    }
    
    // Bridge 정리를 위한 DisposableEffect
    DisposableEffect(jsBridge) {
        onDispose {
            // Composable이 dispose될 때 bridge 정리
            jsBridge.destroy()
        }
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