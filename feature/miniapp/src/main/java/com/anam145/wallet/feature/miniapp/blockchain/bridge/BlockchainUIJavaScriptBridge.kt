package com.anam145.wallet.feature.miniapp.blockchain.bridge

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.ComponentActivity

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
    
    @JavascriptInterface
    fun log(message: String) {
        // JavaScript 로그 (필요시에만 활성화)
        // Log.d("BlockchainUI", "JS: $message")
    }
    
    @JavascriptInterface
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