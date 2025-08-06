package com.anam145.wallet.feature.browser.bridge

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.ComponentActivity

/**
 * Browser JavaScript Bridge
 * 
 * WebView와 Native 간의 통신을 담당합니다.
 * Universal Bridge를 통해 모든 블록체인 요청을 동적으로 처리합니다.
 */
class BrowserJavaScriptBridge(
    private val context: Context,
    private val onUniversalRequest: ((String, String) -> Unit)? = null
) {
    
    companion object {
        private const val TAG = "BrowserJSBridge"
    }
    
    var webView: WebView? = null
    
    /**
     * Universal Bridge 메서드
     * 
     * 모든 블록체인 요청을 처리하는 단일 진입점입니다.
     * Native는 payload의 내용을 파싱하지 않고 그대로 전달합니다.
     * 
     * @param requestId 요청 고유 ID
     * @param payload JSON 문자열 형태의 요청 데이터
     */
    @JavascriptInterface
    fun universalBridge(requestId: String, payload: String) {
        Log.d(TAG, "Universal bridge request: $requestId")
        Log.d(TAG, "Payload: $payload")
        
        try {
            (context as? ComponentActivity)?.runOnUiThread {
                onUniversalRequest?.invoke(requestId, payload)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in universal bridge", e)
            // 에러 응답 전송
            sendUniversalResponse(
                requestId, 
                """{"error": "${e.message}"}"""
            )
        }
    }
    
    /**
     * Universal Bridge 응답 전송
     * 
     * Native에서 JavaScript로 응답을 전송합니다.
     * 
     * @param requestId 요청 ID
     * @param response JSON 문자열 형태의 응답 데이터
     */
    fun sendUniversalResponse(requestId: String, response: String) {
        (context as? ComponentActivity)?.runOnUiThread {
            // response를 JSON 문자열로 escape 처리
            val escapedResponse = response
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029")
            
            // Android에서 JSON 파싱하여 객체로 전달
            val script = """
                (function(){
                    if (window._anamBridge && window._anamBridge.handleResponse) {
                        try {
                            const outer = JSON.parse("$escapedResponse");
                            // 엔벨로프 구조인지 확인하고 풀어내기
                            const rpcResponse = (outer && typeof outer === 'object' && 'responseData' in outer)
                                ? JSON.parse(outer.responseData)  // 엔벨로프 언랩
                                : outer;                          // 이미 순수 RPC 객체
                            
                            window._anamBridge.handleResponse('$requestId', rpcResponse);
                        } catch (e) {
                            console.error('[Bridge] Failed to parse response:', e);
                            window._anamBridge.handleResponse('$requestId', {
                                jsonrpc: "2.0",
                                id: "$requestId",
                                error: { code: -32700, message: "Parse error: " + e.message }
                            });
                        }
                    } else {
                        console.error('[Bridge] Universal Bridge not ready');
                    }
                })();
            """.trimIndent()
            
            webView?.evaluateJavascript(script) { result ->
                Log.d(TAG, "Response sent for $requestId: $result")
            }
        }
    }
}