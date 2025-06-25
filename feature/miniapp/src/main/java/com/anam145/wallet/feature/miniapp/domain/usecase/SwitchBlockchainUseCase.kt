package com.anam145.wallet.feature.miniapp.domain.usecase

import android.util.Log
import com.anam145.wallet.feature.miniapp.IBlockchainService
import com.anam145.wallet.core.common.result.MiniAppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 블록체인 전환을 담당하는 UseCase
 * 
 * 현재 활성화된 블록체인을 다른 블록체인으로 전환합니다.
 * 이 UseCase는 블록체인 전환의 단일 진입점 역할을 하며,
 * Clean Architecture 원칙에 따라 Presentation Layer와 Data Layer를 분리합니다.
 */
@Singleton
class SwitchBlockchainUseCase @Inject constructor() {
    
    companion object {
        private const val TAG = "SwitchBlockchainUseCase"
    }
    
    /**
     * 블록체인을 전환합니다.
     * 
     * @param blockchainId 전환할 블록체인의 ID
     * @param service 블록체인 서비스 인스턴스
     * @return 전환 성공 여부
     */
    suspend operator fun invoke(
        blockchainId: String, 
        service: IBlockchainService
    ): MiniAppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            service.switchBlockchain(blockchainId)
            Log.d(TAG, "Successfully switched to blockchain: $blockchainId")
            MiniAppResult.Success(Unit)
        } catch (e: android.os.DeadObjectException) {
            Log.e(TAG, "Service died while switching blockchain", e)
            MiniAppResult.Error.Unknown(Exception("블록체인 서비스 연결이 끊어졌습니다"))
        } catch (e: android.os.RemoteException) {
            Log.e(TAG, "Remote exception switching blockchain", e)
            MiniAppResult.Error.Unknown(Exception("블록체인 전환 중 오류가 발생했습니다"))
        } catch (e: Exception) {
            Log.e(TAG, "Error switching blockchain", e)
            MiniAppResult.Error.Unknown(e)
        }
    }
}