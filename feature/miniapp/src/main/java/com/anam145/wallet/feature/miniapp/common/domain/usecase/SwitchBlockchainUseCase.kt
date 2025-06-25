package com.anam145.wallet.feature.miniapp.common.domain.usecase

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
            MiniAppResult.Success(Unit)
        } catch (e: android.os.DeadObjectException) {
            // 서비스가 죽었을 때 - 원본 예외도 포함
            MiniAppResult.Error.Unknown(Exception("블록체인 서비스 연결이 끊어졌습니다", e))
        } catch (e: android.os.RemoteException) {
            // IPC 통신 에러 - 원본 예외도 포함
            MiniAppResult.Error.Unknown(Exception("블록체인 전환 중 오류가 발생했습니다", e))
        } catch (e: Exception) {
            // 기타 예외는 그대로 전달
            MiniAppResult.Error.Unknown(e)
        }
    }
}