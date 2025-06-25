package com.anam145.wallet.feature.miniapp.common.domain.model

/**
 * 결제 요청 도메인 모델
 * 
 * @property requestId 요청 ID
 * @property blockchainId 블록체인 ID
 * @property amount 결제 금액
 * @property description 결제 설명
 * @property metadata 추가 메타데이터
 */
data class PaymentRequest(
    val requestId: String,
    val blockchainId: String,
    val amount: String,
    val description: String? = null,
    val metadata: Map<String, Any> = emptyMap()
) {
    /**
     * JSON 문자열로 변환
     */
    fun toJson(): String {
        val json = buildMap {
            put("requestId", requestId)
            put("blockchainId", blockchainId)
            put("amount", amount)
            description?.let { put("description", it) }
            if (metadata.isNotEmpty()) {
                put("metadata", metadata)
            }
        }
        return org.json.JSONObject(json).toString()
    }
}