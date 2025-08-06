package com.anam145.wallet.feature.browser.domain.repository

/**
 * Universal Bridge Repository 인터페이스
 * 
 * Universal Bridge를 통한 블록체인 통신을 담당합니다.
 */
interface UniversalRepository {
    /**
     * Universal Bridge 메서드 요청
     * 
     * @param requestId 요청 고유 ID
     * @param blockchainId 대상 블록체인 ID
     * @param payload JSON 형태의 요청 데이터
     * @return JSON 형태의 응답 데이터
     */
    suspend fun requestMethod(
        requestId: String,
        blockchainId: String,
        payload: String
    ): String
}