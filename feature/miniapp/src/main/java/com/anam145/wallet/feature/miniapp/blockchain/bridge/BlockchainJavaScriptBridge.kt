package com.anam145.wallet.feature.miniapp.blockchain.bridge

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface

/**
 * 블록체인 서비스용 JavaScript Bridge
 * 
 * BlockchainService의 헤드리스 WebView에서 사용됩니다.
 * 트랜잭션 응답을 처리하는 역할을 합니다.
 */
class BlockchainJavaScriptBridge(
    private val onResponse: (String, String) -> Unit
) {
    companion object {
        private const val TAG = "BlockchainService"
    }
    
    private val handler = Handler(Looper.getMainLooper())
    
    @JavascriptInterface
    fun sendTransactionResponse(requestId: String, responseJson: String) {
        Log.d(TAG, "sendTransactionResponse from blockchain: $requestId")
        handler.post {
            onResponse(requestId, responseJson)
        }
    }
    
    /**
     * Universal Bridge 응답 전송
     * 
     * 블록체인 미니앱에서 Universal Bridge 요청에 대한 응답을 전송합니다.
     */
    @JavascriptInterface
    fun sendUniversalResponse(requestId: String, responseJson: String) {
        Log.d(TAG, "sendUniversalResponse from blockchain: $requestId")
        handler.post {
            onResponse(requestId, responseJson)
        }
    }
    
    @JavascriptInterface
    fun log(message: String) {
        Log.d(TAG, "Blockchain JS: $message")
    }
}