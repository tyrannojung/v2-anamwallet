package com.anam145.wallet.feature.miniapp.blockchain.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat
import androidx.webkit.WebViewAssetLoader
import com.anam145.wallet.feature.miniapp.IBlockchainCallback
import com.anam145.wallet.feature.miniapp.IBlockchainService
import com.anam145.wallet.feature.miniapp.common.webview.WebViewFactory
import com.anam145.wallet.core.common.extension.resolveEntryPoint
import com.anam145.wallet.feature.miniapp.common.data.common.MiniAppFileManager
import com.anam145.wallet.feature.miniapp.blockchain.bridge.BlockchainJavaScriptBridge
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

/**
 * 블록체인 처리를 위한 ForegroundService
 * 
 * 별도 프로세스(:blockchain)에서 실행되며 헤드리스 WebView를 통해
 * 블록체인 트랜잭션을 처리합니다.
 */
@AndroidEntryPoint
class BlockchainService : Service() {
    
    @Inject
    lateinit var fileManager: MiniAppFileManager
    
    companion object {
        private const val TAG = "BlockchainService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "blockchain_service"
        private const val CHANNEL_NAME = "Blockchain Service"
        private const val CALLBACK_TIMEOUT_MS = 30_000L
    }
    
    /**
     * 콜백 처리를 위한 Sealed Class
     */
    sealed class CallbackAction {
        data class Register(
            val requestId: String,
            val callback: IBlockchainCallback,
            val requestJson: String
        ) : CallbackAction()
        
        data class Complete(
            val requestId: String,
            val responseJson: String
        ) : CallbackAction()
        
        data class Error(
            val requestId: String,
            val errorMessage: String,
            val errorCode: String? = null
        ) : CallbackAction()
        
        data class Timeout(val requestId: String) : CallbackAction()
        
        object Cleanup : CallbackAction()
    }
    
    // AIDL 바인더
    private val binder = object : IBlockchainService.Stub() {
        override fun switchBlockchain(blockchainId: String?) {
            Log.d(TAG, "switchBlockchain: $blockchainId (thread: ${Thread.currentThread().name})")
            blockchainId?.let {
                // 블록체인 전환 시 WebView만 재생성 (서비스는 유지)
                createBlockchainWebView(it)
            }
        }
        
        override fun getActiveBlockchainId(): String? {
            // Binder 스레드에서 안전하게 반환
            return this@BlockchainService.activeBlockchainId
        }
        
        override fun processRequest(requestJson: String?, callback: IBlockchainCallback?) {
            Log.d(TAG, "processRequest: $requestJson")
            
            if (requestJson == null || callback == null) {
                Log.e(TAG, "Invalid request or callback")
                callback?.onError("""{"error": "Invalid request"}""")
                return
            }
            
            try {
                val requestData = JSONObject(requestJson)
                val requestId = requestData.optString("requestId", 
                    "req_${System.currentTimeMillis()}")
                
                // Channel을 통해 콜백 등록 요청 전송
                serviceScope.launch {
                    callbackChannel.send(
                        CallbackAction.Register(requestId, callback, requestJson)
                    )
                }
                
                // WebView에 이벤트 전달
                handler.post {
                    val script = """
                        (function() {
                            const event = new CustomEvent('transactionRequest', {
                                detail: $requestJson
                            });
                            window.dispatchEvent(event);
                        })();
                    """.trimIndent()
                    
                    activeBlockchainWebView?.evaluateJavascript(script) { result ->
                        Log.d(TAG, "Event dispatched to blockchain: $result")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing request", e)
                try {
                    callback.onError("""{"error": "${e.message ?: "Unknown error"}"}""")
                } catch (remoteEx: RemoteException) {
                    Log.e(TAG, "Failed to send error callback", remoteEx)
                }
            }
        }
        
        override fun isReady(): Boolean {
            return activeBlockchainWebView != null
        }
    }
    
    // 메인 스레드 핸들러
    private val handler = Handler(Looper.getMainLooper())
    
    // 활성 블록체인 정보
    private var activeBlockchainId: String? = null
    private var activeBlockchainWebView: WebView? = null
    
    // 서비스 수명주기를 위한 CoroutineScope
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // 콜백 처리를 위한 Channel
    private val callbackChannel = Channel<CallbackAction>(Channel.UNLIMITED)
    
    // Thread-safe 콜백 저장소 (읽기 전용으로만 사용)
    private val pendingCallbacks = ConcurrentHashMap<String, IBlockchainCallback>()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BlockchainService onCreate")
        
        // 알림 채널 생성 (먼저 실행)
        createNotificationChannel()
        
        // ForegroundService 즉시 시작 (Android 12+ 5초 제한)
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Channel 처리자 시작
        startCallbackProcessor()
        
        // WebView 데이터 디렉토리 설정은 이제 Application.onCreate()에서 처리됨
        // ProcessUtil을 통해 프로세스를 식별하고 적절한 suffix를 설정
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        
        // 활성 블록체인 ID가 있으면 WebView 생성
        intent?.getStringExtra("blockchainId")?.let { blockchainId ->
            Log.d(TAG, "Creating WebView for blockchain: $blockchainId")
            handler.post {
                createBlockchainWebView(blockchainId)
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        
        // WebView 정리
        activeBlockchainWebView?.destroy()
        activeBlockchainWebView = null
        
        // 정리 액션 전송 후 scope 취소
        serviceScope.launch {
            callbackChannel.send(CallbackAction.Cleanup)
            callbackChannel.close()
        }
        
        // Coroutine scope 취소
        serviceScope.cancel()
    }
    
    /**
     * 알림 채널 생성 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "블록체인 서비스 실행 중"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    
    /**
     * 알림 생성
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Anam Wallet")
            .setContentText("블록체인 서비스 실행 중")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: 실제 아이콘으로 변경
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
    
    /**
     * 블록체인 WebView 생성
     */
    private fun createBlockchainWebView(blockchainId: String) {
        handler.post {
            try {
                // 기존 WebView 정리
                activeBlockchainWebView?.destroy()
                
                // JavaScript Bridge 생성
                val bridge = BlockchainJavaScriptBridge { requestId, responseJson ->
                    handleBlockchainResponse(requestId, responseJson)
                }
                
                // WebViewFactory를 사용하여 WebView 생성
                val webView = WebViewFactory.create(
                    context = applicationContext,
                    appId = blockchainId,
                    baseDir = File(fileManager.getMiniAppBasePath(blockchainId)),
                    headless = true,
                    jsBridge = bridge,
                    enableDebugging = false
                )
                
                // 블록체인 앱 로드 - manifest에서 페이지 경로 가져오기
                handler.post {
                    loadBlockchainApp(blockchainId, webView)
                }
                
                activeBlockchainWebView = webView
                activeBlockchainId = blockchainId
                
                Log.d(TAG, "Blockchain WebView created for: $blockchainId with origin: https://$blockchainId.miniapp.local")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating blockchain WebView", e)
            }
        }
    }
    
    /**
     * Manifest를 로드하고 블록체인 앱을 실행
     */
    private fun loadBlockchainApp(blockchainId: String, webView: WebView) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = fileManager.loadManifest(blockchainId)) {
                is com.anam145.wallet.core.common.result.MiniAppResult.Success -> {
                    val manifest = result.data
                    val mainPage = manifest.resolveEntryPoint()
                    
                    handler.post {
                        // WebView는 도메인을 소문자로 변환하므로 일치시킴
                        val httpsUrl = "https://${blockchainId.lowercase()}.miniapp.local/$mainPage"
                        Log.d(TAG, "Loading blockchain app: $httpsUrl")
                        webView.loadUrl(httpsUrl)
                    }
                }
                is com.anam145.wallet.core.common.result.MiniAppResult.Error -> {
                    Log.e(TAG, "Failed to load manifest for $blockchainId: $result")
                    // Fallback to index.html
                    handler.post {
                        val httpsUrl = "https://$blockchainId.miniapp.local/index.html"
                        webView.loadUrl(httpsUrl)
                    }
                }
            }
        }
    }
    
    /**
     * Channel 처리자 시작
     */
    private fun startCallbackProcessor() {
        serviceScope.launch {
            for (action in callbackChannel) {
                when (action) {
                    is CallbackAction.Register -> {
                        Log.d(TAG, "Registering callback for ${action.requestId}")
                        pendingCallbacks[action.requestId] = action.callback
                        
                        // 타임아웃 설정
                        launch {
                            delay(CALLBACK_TIMEOUT_MS)
                            if (pendingCallbacks.containsKey(action.requestId)) {
                                callbackChannel.send(CallbackAction.Timeout(action.requestId))
                            }
                        }
                    }
                    
                    is CallbackAction.Complete -> {
                        Log.d(TAG, "Completing callback for ${action.requestId}")
                        pendingCallbacks.remove(action.requestId)?.let { callback ->
                            try {
                                callback.onSuccess(action.responseJson)
                            } catch (e: RemoteException) {
                                Log.e(TAG, "Client process died", e)
                            }
                        }
                    }
                    
                    is CallbackAction.Error -> {
                        Log.d(TAG, "Error callback for ${action.requestId}")
                        pendingCallbacks.remove(action.requestId)?.let { callback ->
                            try {
                                val errorJson = JSONObject().apply {
                                    put("error", action.errorMessage)
                                    action.errorCode?.let { put("code", it) }
                                }.toString()
                                callback.onError(errorJson)
                            } catch (e: RemoteException) {
                                Log.e(TAG, "Client process died", e)
                            }
                        }
                    }
                    
                    is CallbackAction.Timeout -> {
                        Log.w(TAG, "Timeout for ${action.requestId}")
                        pendingCallbacks.remove(action.requestId)?.let { callback ->
                            try {
                                callback.onError("""{"error": "Request timeout", "code": "TIMEOUT"}""")
                            } catch (e: RemoteException) {
                                Log.e(TAG, "Client process died", e)
                            }
                        }
                    }
                    
                    is CallbackAction.Cleanup -> {
                        Log.d(TAG, "Cleaning up pending callbacks")
                        pendingCallbacks.forEach { (id, callback) ->
                            try {
                                callback.onError("""{"error": "Service destroyed", "code": "SERVICE_DESTROYED"}""")
                            } catch (e: RemoteException) {
                                // 무시
                            }
                        }
                        pendingCallbacks.clear()
                        break // Channel 처리 종료
                    }
                }
            }
            Log.d(TAG, "Callback processor terminated")
        }
    }
    
    /**
     * 블록체인으로부터의 응답 처리
     */
    private fun handleBlockchainResponse(requestId: String, responseJson: String) {
        Log.d(TAG, "handleBlockchainResponse: $requestId - $responseJson")
        
        serviceScope.launch {
            try {
                val response = JSONObject(responseJson)
                if (response.has("error")) {
                    callbackChannel.send(
                        CallbackAction.Error(
                            requestId, 
                            response.getString("error"),
                            if (response.has("code")) response.getString("code") else null
                        )
                    )
                } else {
                    // TransactionResponse 형식으로 래핑
                    val transactionResponse = JSONObject().apply {
                        put("requestId", requestId)
                        put("status", "success")
                        // 원본 응답을 그대로 문자열로 저장
                        put("responseData", responseJson)
                    }.toString()
                    
                    callbackChannel.send(
                        CallbackAction.Complete(requestId, transactionResponse)
                    )
                }
            } catch (e: Exception) {
                callbackChannel.send(
                    CallbackAction.Error(requestId, e.message ?: "Unknown error")
                )
            }
        }
    }
    
}