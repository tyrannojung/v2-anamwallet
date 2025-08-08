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
 * 
 * 보안 설계:
 * - universalBridge 메서드만 @JavascriptInterface를 가지고 있어 JavaScript에 노출됨
 * - sendUniversalResponse와 webView는 의도적으로 JavaScript에 노출하지 않음
 * - 이를 통해 JavaScript → Native는 제한된 인터페이스만 사용하도록 강제
 * - Native → JavaScript는 evaluateJavascript를 통해 안전하게 실행
 * 
 * Android 4.2 (API 17) 이상에서는 @JavascriptInterface가 없는 메서드는
 * JavaScript에서 접근할 수 없으므로 보안상 안전합니다.
 */
class BrowserJavaScriptBridge(
    private val context: Context,
    private val onUniversalRequest: ((String, String) -> Unit)? = null
) {
    
    companion object {
        private const val TAG = "BrowserJSBridge"
    }
    
    /**
     * WebView 참조를 저장합니다.
     * 
     * 주의: 이 프로퍼티는 @JavascriptInterface가 없으므로 JavaScript에서 접근할 수 없습니다.
     * 이는 의도적인 설계로, Native에서만 사용됩니다.
     */
    var webView: WebView? = null
    
    /**
     * Universal Bridge 메서드
     * 
     * JavaScript에서 Native로 통신하는 유일한 진입점입니다.
     * 이 메서드만 @JavascriptInterface를 가지고 있어 JavaScript에서 호출 가능합니다.
     * Native는 payload의 내용을 파싱하지 않고 그대로 전달합니다.
     * 
     * JavaScript 호출 예시:
     * ```javascript
     * window._native.universalBridge(requestId, payload)
     * ```
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
     * 주의: 이 메서드는 @JavascriptInterface가 없으므로 JavaScript에서 직접 호출할 수 없습니다.
     * 이는 보안을 위한 의도적인 설계입니다. Native → JavaScript 단방향 통신만 허용합니다.
     * JavaScript로의 응답은 evaluateJavascript를 통해 안전하게 전달됩니다.
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
            
            // v2.0: WalletBridge만 사용
            val script = """
                (function(){
                    if (window.WalletBridge && window.WalletBridge.handleResponse) {
                        try {
                            const outer = JSON.parse("$escapedResponse");
                            // 엔벨로프 구조인지 확인하고 풀어내기
                            const rpcResponse = (outer && typeof outer === 'object' && 'responseData' in outer)
                                ? JSON.parse(outer.responseData)  // 엔벨로프 언랩
                                : outer;                          // 이미 순수 RPC 객체
                            
                            window.WalletBridge.handleResponse('$requestId', rpcResponse);
                        } catch (e) {
                            console.error('[Bridge] Failed to parse response:', e);
                            window.WalletBridge.handleResponse('$requestId', {
                                jsonrpc: "2.0",
                                id: "$requestId",
                                error: { code: -32700, message: "Parse error: " + e.message }
                            });
                        }
                    } else {
                        console.error('[Bridge] WalletBridge not ready');
                    }
                })();
            """.trimIndent()
            
            webView?.evaluateJavascript(script) { result ->
                Log.d(TAG, "Response sent for $requestId: $result")
            }
        }
    }
}