package com.anam145.wallet.feature.browser.domain.usecase

import com.anam145.wallet.feature.browser.domain.repository.UniversalRepository
import javax.inject.Inject

/**
 * Universal Bridge 메서드 요청 UseCase
 * 
 * 브라우저에서 발생한 Universal Bridge 요청을 처리하고 응답을 반환합니다.
 */
class RequestUniversalMethodUseCase @Inject constructor(
    private val universalRepository: UniversalRepository
) {
    suspend operator fun invoke(
        requestId: String,
        blockchainId: String,
        payload: String
    ): String {
        return universalRepository.requestMethod(
            requestId = requestId,
            blockchainId = blockchainId,
            payload = payload
        )
    }
}