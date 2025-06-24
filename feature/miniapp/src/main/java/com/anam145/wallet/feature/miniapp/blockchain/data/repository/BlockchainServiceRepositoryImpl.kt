package com.anam145.wallet.feature.miniapp.blockchain.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.IBlockchainService
import com.anam145.wallet.feature.miniapp.blockchain.service.BlockchainService
import com.anam145.wallet.feature.miniapp.blockchain.domain.repository.BlockchainServiceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BlockchainServiceRepository 구현체
 * 
 * BlockchainService와의 실제 통신을 처리합니다.
 */
@Singleton
class BlockchainServiceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BlockchainServiceRepository {
    
    companion object {
        private const val TAG = "BlockchainServiceRepository"
    }
    
    private val _serviceConnection = MutableStateFlow<IBlockchainService?>(null)
    private val _isConnected = MutableStateFlow(false)
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Service connected")
            val blockchainService = IBlockchainService.Stub.asInterface(service)
            _serviceConnection.value = blockchainService
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
            val serviceIntent = Intent(context, BlockchainService::class.java)
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
    
    override suspend fun switchBlockchain(blockchainId: String): MiniAppResult<Unit> {
        val service = _serviceConnection.value 
            ?: return MiniAppResult.Error.Unknown(Exception("Service not connected"))
            
        return try {
            service.switchBlockchain(blockchainId)
            MiniAppResult.Success(Unit)
        } catch (e: RemoteException) {
            Log.e(TAG, "Remote exception switching blockchain", e)
            MiniAppResult.Error.Unknown(e)
        }
    }
    
    override suspend fun getActiveBlockchainId(): MiniAppResult<String> {
        val service = _serviceConnection.value 
            ?: return MiniAppResult.Error.Unknown(Exception("Service not connected"))
            
        return try {
            val activeId = service.getActiveBlockchainId()
            if (activeId != null) {
                MiniAppResult.Success(activeId)
            } else {
                MiniAppResult.Error.Unknown(Exception("No active blockchain"))
            }
        } catch (e: RemoteException) {
            Log.e(TAG, "Remote exception getting active blockchain", e)
            MiniAppResult.Error.Unknown(e)
        }
    }
}