package com.anam145.wallet.feature.miniapp.domain.usecase

import com.anam145.wallet.core.common.result.MiniAppResult
import com.anam145.wallet.feature.miniapp.domain.model.PaymentRequest
import com.anam145.wallet.feature.miniapp.domain.model.PaymentResponse
import com.anam145.wallet.feature.miniapp.webapp.domain.repository.WebAppServiceRepository
import javax.inject.Inject

/**
 * 결제 요청을 처리하는 UseCase
 * 
 * 미니앱의 결제 요청을 블록체인 서비스로 전달하고 응답을 받습니다.
 */
class RequestPaymentUseCase @Inject constructor(
    private val repository: WebAppServiceRepository
) {
    /**
     * 결제를 요청합니다.
     * 
     * @param request 결제 요청 정보
     * @return 결제 응답 결과
     */
    suspend operator fun invoke(request: PaymentRequest): MiniAppResult<PaymentResponse> {
        return repository.requestPayment(request)
    }
}