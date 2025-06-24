package com.anam145.wallet.feature.miniapp.webapp.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.anam145.wallet.feature.miniapp.IBlockchainCallback
import com.anam145.wallet.feature.miniapp.IBlockchainService
import com.anam145.wallet.feature.miniapp.IWebAppService
import com.anam145.wallet.feature.miniapp.blockchain.service.BlockchainService
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

/**
 * 웹앱 서비스 - 메인 프로세스에서 실행
 * 
 * 일반 웹앱(정부24 등)과 블록체인 서비스 간의 브릿지 역할을 합니다.
 * 웹앱 프로세스(:app)에서의 요청을 받아 블록체인 프로세스(:blockchain)로 전달합니다.
 */
@AndroidEntryPoint
class WebAppService : Service() {
    
    companion object {
        private const val TAG = "WebAppService"
    }
    
    private var blockchainService: IBlockchainService? = null
    private var isBlockchainServiceBound = false
    
    // 블록체인 서비스 연결
    private val blockchainServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Connected to BlockchainService")
            blockchainService = IBlockchainService.Stub.asInterface(service)
            isBlockchainServiceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Disconnected from BlockchainService")
            blockchainService = null
            isBlockchainServiceBound = false
        }
    }
    
    // AIDL 인터페이스 구현
    private val binder = object : IWebAppService.Stub() {
        
        override fun requestPayment(requestJson: String, callback: IBlockchainCallback) {
            Log.d(TAG, "Payment request received: $requestJson")
            
            try {
                // 블록체인 서비스가 연결되어 있지 않으면 에러 반환
                if (!isBlockchainServiceBound || blockchainService == null) {
                    callback.onError(JSONObject().apply {
                        put("error", "BlockchainService not connected")
                        put("code", "SERVICE_NOT_CONNECTED")
                        put("userMessage", "블록체인 서비스가 연결되지 않았습니다")
                    }.toString())
                    return
                }
                
                // 요청을 블록체인 서비스로 전달
                val request = JSONObject(requestJson)
                val blockchainId = request.optString("blockchainId")
                
                // 현재 활성화된 블록체인과 다르면 전환
                val activeBlockchainId = blockchainService?.getActiveBlockchainId()
                
                // 활성화된 블록체인이 없으면 에러 반환
                if (activeBlockchainId.isNullOrEmpty()) {
                    callback.onError(JSONObject().apply {
                        put("error", "No active blockchain")
                        put("code", "NO_ACTIVE_BLOCKCHAIN")
                        put("userMessage", "활성화된 블록체인이 없습니다. 먼저 블록체인 앱을 실행해주세요.")
                    }.toString())
                    return
                }
                
                if (activeBlockchainId != blockchainId) {
                    Log.d(TAG, "Switching blockchain from $activeBlockchainId to $blockchainId")
                    blockchainService?.switchBlockchain(blockchainId)
                }
                
                // 결제 요청 처리
                blockchainService?.processRequest(requestJson, callback)
                
            } catch (e: RemoteException) {
                Log.e(TAG, "RemoteException in requestPayment", e)
                try {
                    callback.onError(JSONObject().apply {
                        put("error", "Remote exception: ${e.message}")
                        put("code", "REMOTE_EXCEPTION")
                    }.toString())
                } catch (callbackError: RemoteException) {
                    Log.e(TAG, "Failed to send error callback", callbackError)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing payment request", e)
                try {
                    callback.onError(JSONObject().apply {
                        put("error", e.message ?: "Unknown error")
                        put("code", "PROCESSING_ERROR")
                    }.toString())
                } catch (callbackError: RemoteException) {
                    Log.e(TAG, "Failed to send error callback", callbackError)
                }
            }
        }
        
        override fun getActiveBlockchainId(): String? {
            return try {
                blockchainService?.getActiveBlockchainId()
            } catch (e: RemoteException) {
                Log.e(TAG, "RemoteException in getActiveBlockchainId", e)
                null
            }
        }
        
        override fun isReady(): Boolean {
            return isBlockchainServiceBound && blockchainService != null
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MiniAppService created")
        
        // 블록체인 서비스에 연결
        connectToBlockchainService()
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "MiniAppService bound")
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MiniAppService destroyed")
        
        // 블록체인 서비스 연결 해제
        if (isBlockchainServiceBound) {
            unbindService(blockchainServiceConnection)
            isBlockchainServiceBound = false
        }
    }
    
    private fun connectToBlockchainService() {
        val intent = Intent(this, BlockchainService::class.java)
        bindService(intent, blockchainServiceConnection, Context.BIND_AUTO_CREATE)
    }
}