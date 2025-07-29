package com.anam145.wallet.feature.miniapp.webapp.domain.usecase

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.anam145.wallet.feature.miniapp.ICredentialService
import com.anam145.wallet.feature.miniapp.IVPCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * AIDL을 통해 Main 프로세스에서 VP를 생성하는 UseCase
 */
class CreateVPFromServiceUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "CreateVPFromServiceUseCase"
    }
    
    /**
     * VP 생성
     * 
     * @param credentialId 선택된 신분증 ID
     * @param challenge VP 요청의 challenge
     * @return VP JSON 문자열
     */
    suspend operator fun invoke(
        credentialId: String,
        challenge: String
    ): Result<String> = suspendCancellableCoroutine { continuation ->
        
        var service: ICredentialService? = null
        var isBound = false
        lateinit var serviceConnection: ServiceConnection
        
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                Log.d(TAG, "CredentialService connected for VP creation")
                
                try {
                    service = ICredentialService.Stub.asInterface(binder)
                    
                    // VP 생성 콜백
                    val callback = object : IVPCallback.Stub() {
                        override fun onSuccess(vpJson: String) {
                            Log.d(TAG, "VP created successfully")
                            continuation.resume(Result.success(vpJson))
                            
                            // 서비스 연결 해제
                            if (isBound) {
                                context.unbindService(serviceConnection)
                                isBound = false
                            }
                        }
                        
                        override fun onError(error: String) {
                            Log.e(TAG, "VP creation failed: $error")
                            continuation.resume(Result.failure(Exception(error)))
                            
                            // 서비스 연결 해제
                            if (isBound) {
                                context.unbindService(serviceConnection)
                                isBound = false
                            }
                        }
                    }
                    
                    // VP 생성 요청
                    service?.createVP(credentialId, challenge, callback)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating VP", e)
                    continuation.resume(Result.failure(e))
                    
                    if (isBound) {
                        context.unbindService(serviceConnection)
                        isBound = false
                    }
                }
            }
            
            override fun onServiceDisconnected(name: ComponentName) {
                Log.d(TAG, "CredentialService disconnected")
                service = null
            }
        }
        
        // 서비스 바인딩
        val intent = Intent().apply {
            setClassName(
                "com.anam145.wallet",
                "com.anam145.wallet.feature.miniapp.webapp.data.service.CredentialService"
            )
        }
        
        try {
            isBound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!isBound) {
                continuation.resume(Result.failure(Exception("Failed to bind CredentialService")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding service", e)
            continuation.resume(Result.failure(e))
        }
        
        // 취소 시 서비스 연결 해제
        continuation.invokeOnCancellation {
            if (isBound) {
                context.unbindService(serviceConnection)
            }
        }
    }
}