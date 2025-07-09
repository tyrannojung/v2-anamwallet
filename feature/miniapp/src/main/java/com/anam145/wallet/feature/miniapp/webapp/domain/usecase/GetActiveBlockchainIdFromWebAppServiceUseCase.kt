package com.anam145.wallet.feature.miniapp.webapp.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.common.domain.usecase.GetActiveBlockchainIdUseCase
import com.anam145.wallet.feature.miniapp.webapp.domain.repository.WebAppServiceRepository
import javax.inject.Inject

/**
 * WebAppService로부터 현재 활성화된 블록체인 ID를 가져오는 UseCase
 * 
 * WebAppViewModel에서 결제 요청 시 활성 블록체인 정보가 필요하고,
 * 헤더에 활성 블록체인 이름을 표시하기 위해 사용합니다.
 */
class GetActiveBlockchainIdFromWebAppServiceUseCase @Inject constructor(
    private val repository: WebAppServiceRepository
) : GetActiveBlockchainIdUseCase {
    
    override suspend operator fun invoke(): MiniAppResult<String> {
        return repository.getActiveBlockchainId()
    }
}