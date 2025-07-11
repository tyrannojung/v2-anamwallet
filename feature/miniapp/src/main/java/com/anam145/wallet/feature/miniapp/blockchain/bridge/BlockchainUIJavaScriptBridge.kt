package com.anam145.wallet.feature.miniapp.blockchain.bridge

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.anam145.wallet.core.common.model.MiniAppManifest
import com.anam145.wallet.feature.miniapp.IKeystoreCallback
import com.anam145.wallet.feature.miniapp.IKeystoreDecryptCallback
import com.anam145.wallet.feature.miniapp.IMainBridgeService
import com.anam145.wallet.feature.miniapp.common.bridge.service.MainBridgeService
import com.google.gson.Gson

/**
 * 블록체인 UI용 JavaScript Bridge
 */
class BlockchainUIJavaScriptBridge(
    private val context: Context,
    private val blockchainId: String,
    private val manifest: MiniAppManifest
) {
    private var webView: WebView? = null
    private var mainBridgeService: IMainBridgeService? = null
    private val gson = Gson()
    
    companion object {
        private const val TAG = "BlockchainUIBridge"
    }
    
    private var isServiceBound = false
    
    init {
        // Service 바인딩을 나중으로 연기
    }
    
    fun setWebView(webView: WebView) {
        this.webView = webView
        // WebView가 설정된 후에 Service 바인딩 시도
        if (!isServiceBound) {
            bindToMainBridgeService()
        }
    }
    
    private fun bindToMainBridgeService() {
        try {
            val intent = Intent(context, MainBridgeService::class.java)
            val bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                Log.e(TAG, "Failed to bind to MainBridgeService")
            } else {
                Log.d(TAG, "Binding to MainBridgeService initiated")
                isServiceBound = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding to MainBridgeService", e)
        }
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mainBridgeService = IMainBridgeService.Stub.asInterface(service)
            Log.d(TAG, "Connected to MainBridgeService")
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            mainBridgeService = null
            Log.d(TAG, "Disconnected from MainBridgeService")
        }
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
                // 쿼리 파라미터 분리
                val parts = pagePath.split("?", limit = 2)
                val path = parts[0]
                val queryString = if (parts.size > 1) "?${parts[1]}" else ""
                
                // manifest.pages 체크
                if (manifest.pages.isNotEmpty()) {
                    val normalizedPath = path.removePrefix("/").removeSuffix(".html")
                    val isAllowed = manifest.pages.any { allowedPage ->
                        val normalizedAllowedPage = allowedPage.removePrefix("/").removeSuffix(".html")
                        normalizedPath == normalizedAllowedPage || 
                        normalizedPath.startsWith("$normalizedAllowedPage/")
                    }
                    
                    if (!isAllowed) {
                        Log.e("BlockchainUI", "Navigation blocked: '$path' is not in manifest.pages")
                        Log.e("BlockchainUI", "Allowed pages: ${manifest.pages}")
                        return@runOnUiThread
                    }
                }
                
                // 현재 URL에서 도메인 추출
                val domain = "https://$blockchainId.miniapp.local/"
                
                // 페이지 경로 정리 (확장자 추가)
                val page = if (path.endsWith(".html")) path else "$path.html"
                
                // 전체 URL 생성
                val fullUrl = domain + page + queryString
                
                Log.d("BlockchainUI", "Navigating to: $fullUrl")
                web.loadUrl(fullUrl)
            } ?: Log.e("BlockchainUI", "WebView is null, cannot navigate")
        } ?: Log.e("BlockchainUI", "Context is not ComponentActivity")
    }
    
    @JavascriptInterface
    fun createKeystore(privateKey: String, address: String) {
        Log.d(TAG, "createKeystore called: address=$address")
        
        if (privateKey.isBlank()) {
            sendKeystoreError("Private key is required")
            return
        }
        
        if (address.isBlank()) {
            sendKeystoreError("Address is required")
            return
        }
        
        val service = mainBridgeService
        if (service == null) {
            sendKeystoreError("Service not connected")
            return
        }
        
        try {
            service.createKeystore(privateKey, address, object : IKeystoreCallback.Stub() {
                override fun onSuccess(keystoreJson: String) {
                    Log.d(TAG, "Keystore created successfully")
                    sendKeystoreResult(true, keystoreJson)
                }
                
                override fun onError(errorMessage: String) {
                    Log.e(TAG, "Keystore creation failed: $errorMessage")
                    sendKeystoreResult(false, errorMessage)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error calling createKeystore", e)
            sendKeystoreError(e.message ?: "Unknown error")
        }
    }
    
    private fun sendKeystoreResult(success: Boolean, data: String) {
        (context as? ComponentActivity)?.runOnUiThread {
            webView?.let { web ->
                val escapedData = gson.toJson(data)
                val script = if (success) {
                    "window.dispatchEvent(new CustomEvent('keystoreCreated', { detail: { success: true, keystore: $escapedData } }));"
                } else {
                    "window.dispatchEvent(new CustomEvent('keystoreCreated', { detail: { success: false, error: $escapedData } }));"
                }
                web.evaluateJavascript(script, null)
            }
        }
    }
    
    private fun sendKeystoreError(error: String) {
        sendKeystoreResult(false, error)
    }
    
    @JavascriptInterface
    fun decryptKeystore(keystoreJson: String) {
        Log.d(TAG, "decryptKeystore called")
        
        if (keystoreJson.isBlank()) {
            sendDecryptError("Keystore JSON is required")
            return
        }
        
        val service = mainBridgeService
        if (service == null) {
            sendDecryptError("Service not connected")
            return
        }
        
        try {
            service.decryptKeystore(keystoreJson, object : IKeystoreDecryptCallback.Stub() {
                override fun onSuccess(address: String, privateKey: String) {
                    Log.d(TAG, "Keystore decrypted successfully")
                    sendDecryptResult(true, address, privateKey, null)
                }
                
                override fun onError(errorMessage: String) {
                    Log.e(TAG, "Keystore decryption failed: $errorMessage")
                    sendDecryptResult(false, null, null, errorMessage)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error calling decryptKeystore", e)
            sendDecryptError(e.message ?: "Unknown error")
        }
    }
    
    private fun sendDecryptResult(success: Boolean, address: String?, privateKey: String?, error: String?) {
        (context as? ComponentActivity)?.runOnUiThread {
            webView?.let { web ->
                val script = if (success) {
                    val addressJson = gson.toJson(address)
                    val privateKeyJson = gson.toJson(privateKey)
                    "window.dispatchEvent(new CustomEvent('keystoreDecrypted', { detail: { success: true, address: $addressJson, privateKey: $privateKeyJson } }));"
                } else {
                    val errorJson = gson.toJson(error)
                    "window.dispatchEvent(new CustomEvent('keystoreDecrypted', { detail: { success: false, error: $errorJson } }));"
                }
                web.evaluateJavascript(script, null)
            }
        }
    }
    
    private fun sendDecryptError(error: String) {
        sendDecryptResult(false, null, null, error)
    }
    
    fun destroy() {
        try {
            if (isServiceBound) {
                context.unbindService(serviceConnection)
                isServiceBound = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding service", e)
        }
    }
}