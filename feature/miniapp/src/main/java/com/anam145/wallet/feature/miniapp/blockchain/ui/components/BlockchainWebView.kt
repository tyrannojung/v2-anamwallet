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
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.common.webview.WebViewFactory
import java.io.File
import org.json.JSONObject

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

    @android.webkit.JavascriptInterface
    fun sendWalletData(walletDataJson: String) {
        Log.d("BlockchainUI", "📨 Received wallet data from JavaScript")

        try {
            val jsonObject = JSONObject(walletDataJson)

            // 🎯 각 키 정보를 로그로 출력
            Log.d("WalletTest", "=".repeat(50))
            Log.d("WalletTest", "🎉 비트코인 지갑 생성 성공!")
            Log.d("WalletTest", "=".repeat(50))
            Log.d("WalletTest", "📍 블록체인: ${jsonObject.getString("blockchain").uppercase()}")
            Log.d("WalletTest", "🌐 네트워크: ${jsonObject.getString("network")}")
            Log.d("WalletTest", "📱 주소: ${jsonObject.getString("address")}")
            Log.d("WalletTest", "🔑 니모닉: ${jsonObject.getString("mnemonic")}")
            Log.d("WalletTest", "🔐 개인키: ${jsonObject.getString("privateKey")}")
            Log.d("WalletTest", "⏰ 생성시간: ${jsonObject.getString("createdAt")}")
            Log.d("WalletTest", "=".repeat(50))
            Log.d("WalletTest", "✅ JavaScript → Kotlin 데이터 전달 완료!")
            Log.d("WalletTest", "=".repeat(50))

        } catch (e: Exception) {
            Log.e("WalletTest", "❌ 지갑 데이터 파싱 실패", e)
        }
    }
}