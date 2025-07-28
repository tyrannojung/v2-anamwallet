package com.anam145.wallet.feature.miniapp.webapp.domain.usecase

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.anam145.wallet.feature.miniapp.ICredentialService
import com.anam145.wallet.feature.miniapp.webapp.domain.model.CredentialInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * AIDL을 통해 Main 프로세스에서 신분증 목록을 조회하는 UseCase
 */
class GetCredentialsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "GetCredentialsUseCase"
        private val gson = Gson()
    }
    
    /**
     * 신분증 목록 조회
     * 
     * @return 신분증 목록
     */
    suspend operator fun invoke(): Result<List<CredentialInfo>> = 
        suspendCancellableCoroutine { continuation ->
            
        var service: ICredentialService? = null
        var isBound = false
        
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                Log.d(TAG, "CredentialService connected")
                
                try {
                    service = ICredentialService.Stub.asInterface(binder)
                    val credentialsJson = service?.getCredentials() ?: "[]"
                    
                    Log.d(TAG, "Received credentials JSON: $credentialsJson")
                    
                    val type = object : TypeToken<List<CredentialInfo>>() {}.type
                    val credentials: List<CredentialInfo> = gson.fromJson(credentialsJson, type)
                    
                    continuation.resume(Result.success(credentials))
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting credentials", e)
                    continuation.resume(Result.failure(e))
                } finally {
                    // 서비스 연결 해제
                    if (isBound) {
                        context.unbindService(this)
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
            isBound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
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
                context.unbindService(connection)
            }
        }
    }
}