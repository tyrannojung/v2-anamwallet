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
        // startService()와 bindService()를 모두 사용하는 이유:
        // 1. startService(): 서비스를 독립적으로 실행시켜 모든 클라이언트가 unbind해도 계속 실행되도록 함
        //                   BlockchainService는 Foreground Service로 앱의 핵심 기능이므로 항상 실행되어야 함
        // 2. bindService(): 서비스와 통신할 수 있는 IBinder(IBlockchainService) 연결을 설정
        // 3. BIND_AUTO_CREATE: 서비스가 아직 생성되지 않았다면 자동으로 생성하고 시작
        // 
        // 이 조합으로 서비스는 다음을 보장합니다:
        // - 서비스가 없으면 자동 생성 (BIND_AUTO_CREATE)
        // - 모든 바인딩이 해제되어도 서비스는 계속 실행 (startService 덕분)
        // - 명시적으로 stopService()를 호출하거나 서비스 내부에서 stopSelf()를 호출할 때까지 유지
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