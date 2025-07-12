package com.anam145.wallet.feature.miniapp.common.bridge.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import com.anam145.wallet.core.security.domain.usecase.DecryptKeystoreUseCase
import com.anam145.wallet.core.security.domain.usecase.GenerateKeystoreUseCase
import com.anam145.wallet.feature.auth.domain.PasswordManager
import com.anam145.wallet.feature.miniapp.IBlockchainCallback
import com.anam145.wallet.feature.miniapp.IBlockchainService
import com.anam145.wallet.feature.miniapp.IKeystoreCallback
import com.anam145.wallet.feature.miniapp.IKeystoreDecryptCallback
import com.anam145.wallet.feature.miniapp.IMainBridgeService
import com.anam145.wallet.feature.miniapp.blockchain.service.BlockchainService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * 메인 브릿지 서비스 - 메인 프로세스에서 실행
 * 
 * 일반 웹앱(정부24 등)과 블록체인 서비스 간의 브릿지 역할을 합니다.
 * 웹앱 프로세스(:app)에서의 요청을 받아 블록체인 프로세스(:blockchain)로 전달합니다.
 */
@AndroidEntryPoint
class MainBridgeService : Service() {
    
    companion object {
        private const val TAG = "MainBridgeService"
    }
    
    @Inject
    lateinit var passwordManager: PasswordManager
    
    @Inject
    lateinit var generateKeystoreUseCase: GenerateKeystoreUseCase
    
    @Inject
    lateinit var decryptKeystoreUseCase: DecryptKeystoreUseCase
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
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
    private val binder = object : IMainBridgeService.Stub() {
        
        override fun requestTransaction(requestJson: String, callback: IBlockchainCallback) {
            Log.d(TAG, "Transaction request received: $requestJson")
            
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
                
                // 요청을 파싱하여 필요한 정보 추출
                val request = JSONObject(requestJson)
                val requestId = request.optString("requestId")
                val blockchainId = request.optString("blockchainId")
                val transactionData = request.optString("transactionData")
                
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
                
                // 블록체인 서비스로 전달할 데이터 준비
                val blockchainRequest = if (transactionData.isNotEmpty()) {
                    // transactionData를 파싱하여 requestId 추가
                    try {
                        val txJson = JSONObject(transactionData)
                        txJson.put("requestId", requestId)
                        txJson.toString()
                    } catch (e: Exception) {
                        // 파싱 실패 시 새로운 JSON 생성
                        JSONObject().apply {
                            put("requestId", requestId)
                            put("data", transactionData)
                        }.toString()
                    }
                } else {
                    // transactionData가 없으면 원본 그대로
                    requestJson
                }
                
                // 결제 요청 처리 - 원본 트랜잭션 데이터만 전달
                blockchainService?.processRequest(blockchainRequest, callback)
                
            } catch (e: RemoteException) {
                Log.e(TAG, "RemoteException in requestTransaction", e)
                try {
                    callback.onError(JSONObject().apply {
                        put("error", "Remote exception: ${e.message}")
                        put("code", "REMOTE_EXCEPTION")
                    }.toString())
                } catch (callbackError: RemoteException) {
                    Log.e(TAG, "Failed to send error callback", callbackError)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing transaction request", e)
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
        
        override fun createKeystore(
            privateKey: String,
            address: String,
            callback: IKeystoreCallback
        ) {
            Log.d(TAG, "createKeystore called for address: $address")
            
            serviceScope.launch {
                try {
                    // 1. 비밀번호 가져오기
                    val password = passwordManager.getPassword()
                    if (password == null) {
                        callback.onError("Not authenticated. Please login first.")
                        return@launch
                    }
                    
                    // 2. 키스토어 생성
                    val result = generateKeystoreUseCase(
                        password = password,
                        address = address,
                        privateKey = privateKey
                    )
                    
                    result.fold(
                        onSuccess = { keystoreJson ->
                            Log.d(TAG, "Keystore created successfully")
                            callback.onSuccess(keystoreJson)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Failed to create keystore", error)
                            callback.onError(error.message ?: "Failed to create keystore")
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error in createKeystore", e)
                    try {
                        callback.onError(e.message ?: "Unknown error")
                    } catch (callbackError: RemoteException) {
                        Log.e(TAG, "Failed to send error callback", callbackError)
                    }
                }
            }
        }
        
        override fun decryptKeystore(
            keystoreJson: String,
            callback: IKeystoreDecryptCallback
        ) {
            Log.d(TAG, "decryptKeystore called")
            
            serviceScope.launch {
                try {
                    // 1. 비밀번호 가져오기
                    val password = passwordManager.getPassword()
                    if (password == null) {
                        callback.onError("Not authenticated. Please login first.")
                        return@launch
                    }
                    
                    // 2. 키스토어 복호화
                    val result = decryptKeystoreUseCase(
                        password = password,
                        keystoreJson = keystoreJson
                    )
                    
                    result.fold(
                        onSuccess = { credentials ->
                            Log.d(TAG, "Keystore decrypted successfully")
                            callback.onSuccess(credentials.address, credentials.privateKey)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Failed to decrypt keystore", error)
                            callback.onError(error.message ?: "Failed to decrypt keystore")
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error in decryptKeystore", e)
                    try {
                        callback.onError(e.message ?: "Unknown error")
                    } catch (callbackError: RemoteException) {
                        Log.e(TAG, "Failed to send error callback", callbackError)
                    }
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MainBridgeService created")
        
        // 블록체인 서비스에 연결
        connectToBlockchainService()
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "MainBridgeService bound")
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainBridgeService destroyed")
        
        // 코루틴 스코프 취소
        serviceScope.cancel()
        
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