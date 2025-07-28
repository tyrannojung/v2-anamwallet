package com.anam145.wallet.feature.miniapp.webapp.bridge

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.anam145.wallet.core.common.model.MiniAppManifest
import org.json.JSONObject

/**
 * WebApp WebView와 Native 간의 통신을 위한 JavaScript Bridge
 * 
 * WebView에서 window.anam 객체로 접근 가능합니다.
 */
class WebAppJavaScriptBridge(
    private val context: Context,
    private val manifest: MiniAppManifest?,
    private val onTransactionRequest: ((JSONObject) -> Unit)? = null,
    private val onVPRequest: ((JSONObject) -> Unit)? = null
) {
    
    // WebView 인스턴스 참조
    var webView: WebView? = null
    
    companion object {
        private const val TAG = "WebAppJSBridge"
    }
    
    /**
     * 트랜잭션 요청
     * JavaScript: window.anam.requestTransaction(jsonString)
     */
    @JavascriptInterface
    fun requestTransaction(transactionDataJson: String) {
        Log.d(TAG, "requestTransaction called with: $transactionDataJson")
        
        try {
            val transactionData = JSONObject(transactionDataJson)
            
            // UI 스레드에서 콜백 실행
            (context as? ComponentActivity)?.runOnUiThread {
                onTransactionRequest?.invoke(transactionData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing transaction data", e)
        }
    }
    
    /**
     * 로그 출력
     * JavaScript: window.anam.log(message)
     */
    @JavascriptInterface
    fun log(message: String) {
        Log.d(TAG, "JS Log: $message")
    }
    
    /**
     * 앱 정보 가져오기
     * JavaScript: window.anam.getAppInfo()
     */
    @JavascriptInterface
    fun getAppInfo(): String {
        return try {
            JSONObject().apply {
                put("appId", manifest?.appId ?: "")
                put("name", manifest?.name ?: "")
                put("version", manifest?.version ?: "")
                put("type", manifest?.type ?: "")
            }.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating app info", e)
            "{}"
        }
    }
    
    /**
     * 페이지 네비게이션
     * JavaScript: window.anam.navigateTo(pagePath)
     */
    @JavascriptInterface
    fun navigateTo(pagePath: String) {
        Log.d(TAG, "navigateTo: $pagePath")
        
        (context as? ComponentActivity)?.runOnUiThread {
            webView?.let { web ->
                val appId = manifest?.appId ?: run {
                    Log.e(TAG, "Manifest or appId is null")
                    return@runOnUiThread
                }
                
                // 쿼리 파라미터 분리
                val parts = pagePath.split("?", limit = 2)
                val path = parts[0]
                val queryString = if (parts.size > 1) "?${parts[1]}" else ""
                
                // manifest.pages 체크
                manifest?.pages?.let { pages ->
                    if (pages.isNotEmpty()) {
                        val normalizedPath = path.removePrefix("/").removeSuffix(".html")
                        val isAllowed = pages.any { allowedPage ->
                            val normalizedAllowedPage = allowedPage.removePrefix("/").removeSuffix(".html")
                            normalizedPath == normalizedAllowedPage || 
                            normalizedPath.startsWith("$normalizedAllowedPage/")
                        }
                        
                        if (!isAllowed) {
                            Log.e(TAG, "Navigation blocked: '$path' is not in manifest.pages")
                            Log.e(TAG, "Allowed pages: $pages")
                            return@runOnUiThread
                        }
                    }
                }
                
                // 현재 URL에서 도메인 추출
                val domain = "https://$appId.miniapp.local/"
                
                // 페이지 경로 정리 (확장자 추가)
                val page = if (path.endsWith(".html")) path else "$path.html"
                
                // 전체 URL 생성
                val fullUrl = domain + page + queryString
                
                Log.d(TAG, "Navigating to: $fullUrl")
                web.loadUrl(fullUrl)
            } ?: Log.e(TAG, "WebView is null, cannot navigate")
        } ?: Log.e(TAG, "Context is not ComponentActivity")
    }
    
    /**
     * VP (Verifiable Presentation) 요청
     * JavaScript: window.anam.requestVP(jsonString)
     */
    @JavascriptInterface
    fun requestVP(vpRequestJson: String) {
        Log.d(TAG, "VP request received: $vpRequestJson")
        
        try {
            val vpRequest = JSONObject(vpRequestJson)
            
            // UI 스레드에서 콜백 실행
            (context as? ComponentActivity)?.runOnUiThread {
                onVPRequest?.invoke(vpRequest)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing VP request", e)
        }
    }
    
    /**
     * JavaScript Bridge 정리
     * WebView 참조를 제거하여 메모리 누수를 방지합니다.
     */
    fun destroy() {
        Log.d(TAG, "Destroying JavaScript Bridge")
        webView = null
    }
}