package com.anam145.wallet.feature.miniapp.common.domain.model

import org.json.JSONObject

/**
 * 트랜잭션 요청 도메인 모델
 * 
 * @property requestId 요청 ID
 * @property blockchainId 블록체인 ID
 * @property transactionData 트랜잭션 데이터 (원본 JSON 문자열)
 */
data class TransactionRequest(
    val requestId: String,
    val blockchainId: String,
    val transactionData: String
) {
    /**
     * JSON 문자열로 변환
     * 
     * MainBridgeService에서 사용하기 위한 래핑된 JSON 생성
     */
    fun toJson(): String {
        return JSONObject().apply {
            put("requestId", requestId)
            put("blockchainId", blockchainId)
            put("transactionData", transactionData)
        }.toString()
    }
}