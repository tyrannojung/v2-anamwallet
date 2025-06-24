package com.anam145.wallet.feature.miniapp.webapp.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.IBlockchainCallback
import com.anam145.wallet.feature.miniapp.IWebAppService
import com.anam145.wallet.feature.miniapp.domain.model.PaymentRequest
import com.anam145.wallet.feature.miniapp.domain.model.PaymentResponse
import com.anam145.wallet.feature.miniapp.webapp.domain.repository.WebAppServiceRepository
import com.anam145.wallet.feature.miniapp.webapp.service.WebAppService
import org.json.JSONObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * WebAppServiceRepository 구현체
 * 
 * AIDL 서비스와의 실제 통신을 처리합니다.
 */
@Singleton
class WebAppServiceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WebAppServiceRepository {
    
    companion object {
        private const val TAG = "WebAppServiceRepository"
    }
    
    private val _serviceConnection = MutableStateFlow<IWebAppService?>(null)
    private val _isConnected = MutableStateFlow(false)
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            val webAppService = IWebAppService.Stub.asInterface(service)
            _serviceConnection.value = webAppService
            _isConnected.value = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            _serviceConnection.value = null
            _isConnected.value = false
        }
    }
    
    override fun observeServiceConnection(): Flow<Boolean> = _isConnected.asStateFlow()
    
    override suspend fun connectToService(): MiniAppResult<Unit> {
        return try {
            val serviceIntent = Intent(context, WebAppService::class.java)
            val bound = context.bindService(
                serviceIntent, 
                serviceConnection, 
                Context.BIND_AUTO_CREATE
            )
            
            if (bound) {
                MiniAppResult.Success(Unit)
            } else {
                MiniAppResult.Error.Unknown(Exception("Failed to bind service"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to service", e)
            MiniAppResult.Error.Unknown(e)
        }
    }
    
    override suspend fun disconnectFromService() {
        try {
            context.unbindService(serviceConnection)
            _serviceConnection.value = null
            _isConnected.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from service", e)
        }
    }
    
    override suspend fun getActiveBlockchainId(): MiniAppResult<String> {
        val service = _serviceConnection.value 
            ?: return MiniAppResult.Error.Unknown(Exception("Service not connected"))
            
        return try {
            val blockchainId = service.getActiveBlockchainId()
            if (!blockchainId.isNullOrEmpty()) {
                MiniAppResult.Success(blockchainId)
            } else {
                MiniAppResult.Error.Unknown(Exception("No active blockchain"))
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "Remote exception getting active blockchain", e)
            MiniAppResult.Error.Unknown(e)
        }
    }
    
    override suspend fun requestPayment(request: PaymentRequest): MiniAppResult<PaymentResponse> {
        val service = _serviceConnection.value 
            ?: return MiniAppResult.Error.Unknown(Exception("Service not connected"))
            
        return suspendCancellableCoroutine { continuation ->
            try {
                service.requestPayment(
                    request.toJson(),
                    object : IBlockchainCallback.Stub() {
                        override fun onSuccess(responseJson: String?) {
                            Log.d(TAG, "Payment success: $responseJson")
                            try {
                                val response = PaymentResponse.fromJson(responseJson ?: "{}")
                                continuation.resume(MiniAppResult.Success(response))
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing payment response", e)
                                continuation.resume(MiniAppResult.Error.Unknown(e))
                            }
                        }
                        
                        override fun onError(errorJson: String?) {
                            Log.e(TAG, "Payment error: $errorJson")
                            
                            // 에러 메시지 파싱하여 더 구체적인 안내 제공
                            val errorMessage = try {
                                val errorObj = JSONObject(errorJson ?: "{}")
                                val error = errorObj.optString("error", "Unknown error")
                                val code = errorObj.optString("code", "")
                                
                                when {
                                    error.contains("No wallet found", ignoreCase = true) -> 
                                        "블록체인 지갑이 생성되지 않았습니다. 먼저 블록체인 앱에서 지갑을 생성해주세요."
                                    error.contains("BlockchainService not connected", ignoreCase = true) ->
                                        "블록체인 서비스가 연결되지 않았습니다. 잠시 후 다시 시도해주세요."
                                    error.contains("No active blockchain", ignoreCase = true) ->
                                        "활성화된 블록체인이 없습니다. 블록체인 앱을 한 번 실행해주세요."
                                    else -> error
                                }
                            } catch (e: Exception) {
                                errorJson ?: "결제 처리 중 오류가 발생했습니다"
                            }
                            
                            continuation.resume(
                                MiniAppResult.Error.Unknown(Exception(errorMessage))
                            )
                        }
                    }
                )
            } catch (e: RemoteException) {
                Log.e(TAG, "Remote exception during payment request", e)
                continuation.resume(MiniAppResult.Error.Unknown(e))
            }
            
            // 취소 시 처리
            continuation.invokeOnCancellation {
                Log.d(TAG, "Payment request cancelled")
            }
        }
    }
}