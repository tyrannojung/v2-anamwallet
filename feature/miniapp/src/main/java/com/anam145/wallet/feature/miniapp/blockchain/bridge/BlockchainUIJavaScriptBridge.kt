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
import com.anam145.wallet.feature.miniapp.IMainBridgeService
import com.anam145.wallet.feature.miniapp.blockchain.ui.components.BlockchainUIJavaScriptBridge
import com.anam145.wallet.feature.miniapp.blockchain.ui.components.BlockchainUIJavaScriptBridge.Companion
import com.anam145.wallet.feature.miniapp.common.bridge.service.MainBridgeService
import org.json.JSONObject

/**
 * 블록체인 UI용 JavaScript Bridge
 */
class BlockchainUIJavaScriptBridge(
    private val context: Context,
    private val blockchainId: String,
    private val manifest: MiniAppManifest
) {
    private var webView: WebView? = null
    companion object {
        private const val TAG = "BlockchainUIBridge"
    }
    private var mainBridgeService: IMainBridgeService? = null
    private var isBound = false
    // MainBridgeService 연결 관리
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mainBridgeService = IMainBridgeService.Stub.asInterface(service)
            isBound = true
            Log.d("블라블라", "✅ MainBridgeService 연결됨")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mainBridgeService = null
            isBound = false
            Log.d("블라블라", "❌ MainBridgeService 연결 해제됨")
        }
    }
    fun setWebView(webView: WebView) {
        this.webView = webView
    }
    
    @JavascriptInterface
    fun log(message: String) {
        // JavaScript 로그 (필요시에만 활성화)
        // Log.d("BlockchainUI", "JS: $message")
    }
    init {
        // MainBridgeService에 연결
        connectToMainBridgeService()
    }

    /**
     * WebView에서 지갑 데이터 수신 및 개인키/주소 추출하여 MainBridgeService로 전달
     */
    @android.webkit.JavascriptInterface
    fun sendWalletData(walletDataJson: String) {
        Log.d(TAG, "📨 WebView로부터 지갑 데이터 수신")

        try {
            val jsonObject = JSONObject(walletDataJson)

            // 기존 로그 출력 (확인용)
            Log.d("WalletReceived", "=".repeat(50))
            Log.d("WalletReceived", "비트코인 지갑 생성 성공")
            Log.d("WalletReceived", "=".repeat(50))
            Log.d("WalletReceived", "블록체인: ${jsonObject.optString("blockchain", "").uppercase()}")
            Log.d("WalletReceived", "네트워크: ${jsonObject.optString("network", "")}")
            Log.d("WalletReceived", "주소: ${jsonObject.optString("address", "")}")
            Log.d("WalletReceived", "니모닉: ${jsonObject.optString("mnemonic", "").take(20)}...")
            Log.d("WalletReceived", "개인키: ${jsonObject.optString("privateKey", "").take(10)}...")
            Log.d("WalletReceived", "생성시간: ${jsonObject.optString("createdAt", "")}")
            Log.d("WalletReceived", "=".repeat(50))

            // 개인키와 주소 추출
            val privateKey = jsonObject.optString("privateKey", "")
            val address = jsonObject.optString("address", "")

            // 추출된 데이터 검증
            if (privateKey.isEmpty()) {
                Log.e(TAG, "❌ 개인키가 비어있습니다!")
                return
            }

            if (address.isEmpty()) {
                Log.e(TAG, "❌ 주소가 비어있습니다!")
                return
            }

            Log.d(TAG, "✅ 개인키와 주소 추출 성공")
            Log.d(TAG, "   - 개인키 길이: ${privateKey.length}")
            Log.d(TAG, "   - 주소: $address")

            // 🚀 MainBridgeService로 전달
            sendToMainBridgeService(privateKey, address)

        } catch (e: Exception) {
            Log.e(TAG, "❌ 지갑 데이터 파싱 실패", e)
        }
    }

    /**
     * MainBridgeService로 개인키와 주소 전달
     */
    private fun sendToMainBridgeService(privateKey: String, address: String) {
        if (!isBound || mainBridgeService == null) {
            Log.e(TAG, "❌ MainBridgeService가 연결되지 않음 - 데이터 전달 실패")
            return
        }

        try {
            Log.d(TAG, "📤 MainBridgeService로 개인키와 주소 전달 중...")

            // AIDL 메서드 호출
            mainBridgeService?.sendPrivateKeyAndAddress(privateKey, address)

            // 이게 찐임
            var result = mainBridgeService?.generateWalletJson(address, privateKey);
            Log.d(TAG, "결과: $result");
            Log.d(TAG, "✅ MainBridgeService로 데이터 전달 완료!")

        } catch (e: Exception) {
            Log.e(TAG, "❌ MainBridgeService 호출 실패", e)
        }
    }

    /**
     * MainBridgeService에 연결
     */
    private fun connectToMainBridgeService() {
        try {
            val intent = Intent(context, MainBridgeService::class.java)
            val bindResult = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

            if (bindResult) {
                Log.d(TAG, "🔗 MainBridgeService 연결 시도 중...")
            } else {
                Log.e(TAG, "❌ MainBridgeService 연결 실패")
            }
        } catch (e: Exception) {
            Log.e(TAG, "MainBridgeService 연결 중 오류", e)
        }
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
}