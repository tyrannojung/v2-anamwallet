package com.anam145.wallet.feature.miniapp.domain.usecase

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.anam145.wallet.feature.miniapp.IBlockchainService
import com.anam145.wallet.feature.miniapp.blockchain.service.BlockchainService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 블록체인 서비스를 활성화하고 관리하는 UseCase
 */
@Singleton
class ActivateBlockchainUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ActivateBlockchainUseCase"
    }
    
    data class BlockchainServiceState(
        val isConnected: Boolean = false,
        val service: IBlockchainService? = null,
        val activeBlockchainId: String? = null
    )
    
    /**
     * 블록체인 서비스 상태를 관찰합니다.
     */
    fun observeServiceState(): Flow<BlockchainServiceState> = callbackFlow {
        var currentService: IBlockchainService? = null
        
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d(TAG, "Blockchain service connected")
                currentService = IBlockchainService.Stub.asInterface(service)
                
                // 즉시 연결 상태만 전송 (AIDL 호출 없이)
                trySend(BlockchainServiceState(
                    isConnected = true,
                    service = currentService,
                    activeBlockchainId = null // 나중에 필요할 때 가져옴
                ))
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "Blockchain service disconnected")
                currentService = null
                trySend(BlockchainServiceState(isConnected = false))
            }
        }
        
        // 서비스 시작 및 바인딩
        val serviceIntent = Intent(context, BlockchainService::class.java)
        context.startService(serviceIntent)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        // 초기 상태 전송
        trySend(BlockchainServiceState(isConnected = false))
        
        // Flow가 취소될 때 정리
        awaitClose {
            context.unbindService(serviceConnection)
        }
    }
    
    /**
     * 블록체인을 전환합니다.
     */
    suspend operator fun invoke(blockchainId: String, service: IBlockchainService?) {
        try {
            service?.switchBlockchain(blockchainId)
            Log.d(TAG, "Switched to blockchain: $blockchainId")
        } catch (e: android.os.DeadObjectException) {
            Log.e(TAG, "Service died while switching blockchain", e)
            throw e // Propagate to caller for handling
        } catch (e: Exception) {
            Log.e(TAG, "Error switching blockchain", e)
            throw e
        }
    }
}