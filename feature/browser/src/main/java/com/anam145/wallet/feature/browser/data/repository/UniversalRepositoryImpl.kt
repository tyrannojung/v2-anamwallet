package com.anam145.wallet.feature.browser.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.anam145.wallet.feature.browser.domain.repository.UniversalRepository
import com.anam145.wallet.feature.miniapp.IMainBridgeService
import com.anam145.wallet.feature.miniapp.IUniversalCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Universal Bridge Repository 구현체
 * 
 * MainBridgeService를 통해 블록체인 프로세스와 통신합니다.
 */
@Singleton
class UniversalRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UniversalRepository {
    
    companion object {
        private const val TAG = "UniversalRepository"
    }
    
    private var mainBridgeService: IMainBridgeService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "MainBridgeService connected")
            mainBridgeService = IMainBridgeService.Stub.asInterface(service)
            isServiceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "MainBridgeService disconnected")
            mainBridgeService = null
            isServiceBound = false
        }
    }
    
    init {
        Log.d(TAG, "UniversalRepositoryImpl initialized")
        // Lazy initialization - 실제 사용 시점에 서비스 연결
    }
    
    override suspend fun requestMethod(
        requestId: String,
        blockchainId: String,
        payload: String
    ): String {
        // 서비스 연결 확인
        ensureServiceConnected()
        
        val service = mainBridgeService
            ?: throw IllegalStateException("MainBridgeService not available")
        
        return suspendCancellableCoroutine { continuation ->
            val callback = object : IUniversalCallback.Stub() {
                override fun onSuccess(requestId: String, result: String) {
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }
                
                override fun onError(requestId: String, error: String) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            UniversalBridgeException(error)
                        )
                    }
                }
            }
            
            try {
                // 서비스 호출
                service.processUniversalRequest(
                    requestId,
                    payload,
                    callback
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to call processUniversalRequest", e)
                continuation.resumeWithException(e)
            }
        }
    }
    
    private suspend fun ensureServiceConnected() {
        if (isServiceBound && mainBridgeService != null) return
        
        withContext(Dispatchers.IO) {
            repeat(3) { attempt ->
                Log.d(TAG, "Attempting to bind MainBridgeService (attempt ${attempt + 1})")
                bindToMainBridgeService()
                
                // 서비스 연결 대기
                repeat(10) {
                    delay(200)
                    if (isServiceBound) {
                        Log.d(TAG, "Service bound successfully")
                        return@withContext
                    }
                }
                
                if (attempt < 2) {
                    Log.d(TAG, "Service binding failed, retrying...")
                    unbindFromMainBridgeService()
                    delay(500)
                }
            }
            
            throw IllegalStateException("Failed to bind MainBridgeService after 3 attempts")
        }
    }
    
    private fun bindToMainBridgeService() {
        val intent = Intent().apply {
            setClassName(
                "com.anam145.wallet",
                "com.anam145.wallet.feature.miniapp.common.bridge.service.MainBridgeService"
            )
        }
        
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun unbindFromMainBridgeService() {
        if (isServiceBound) {
            context.unbindService(serviceConnection)
            isServiceBound = false
            mainBridgeService = null
        }
    }
}

/**
 * Universal Bridge 예외
 */
class UniversalBridgeException(message: String) : Exception(message)