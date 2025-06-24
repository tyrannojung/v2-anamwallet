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
    private val onPaymentRequest: ((JSONObject) -> Unit)? = null
) {
    
    // WebView 인스턴스 참조
    var webView: WebView? = null
    
    companion object {
        private const val TAG = "WebAppJSBridge"
    }
    
    /**
     * 결제 요청
     * JavaScript: window.anam.requestPayment(jsonString)
     */
    @JavascriptInterface
    fun requestPayment(paymentDataJson: String) {
        Log.d(TAG, "requestPayment called with: $paymentDataJson")
        
        try {
            val paymentData = JSONObject(paymentDataJson)
            
            // UI 스레드에서 콜백 실행
            (context as? ComponentActivity)?.runOnUiThread {
                onPaymentRequest?.invoke(paymentData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing payment data", e)
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
                
                // 현재 URL에서 도메인 추출
                val domain = "https://$appId.miniapp.local/"
                
                // 쿼리 파라미터 분리
                val parts = pagePath.split("?", limit = 2)
                val path = parts[0]
                val queryString = if (parts.size > 1) "?${parts[1]}" else ""
                
                // 페이지 경로 정리 (확장자 추가)
                val page = if (path.endsWith(".html")) path else "$path.html"
                
                // 전체 URL 생성
                val fullUrl = domain + page + queryString
                
                Log.d(TAG, "Navigating to: $fullUrl")
                web.loadUrl(fullUrl)
            } ?: Log.e(TAG, "WebView is null, cannot navigate")
        } ?: Log.e(TAG, "Context is not ComponentActivity")
    }
    
    
}