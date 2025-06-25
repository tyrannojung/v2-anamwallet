package com.anam145.wallet.feature.miniapp.common.domain.model

import org.json.JSONObject

/**
 * 결제 응답 도메인 모델
 * 
 * @property requestId 요청 ID
 * @property status 결제 상태 (success, failed, pending)
 * @property transactionId 트랜잭션 ID
 * @property message 응답 메시지
 * @property data 추가 응답 데이터
 */
data class PaymentResponse(
    val requestId: String,
    val status: String,
    val transactionId: String? = null,
    val message: String? = null,
    val data: Map<String, Any> = emptyMap()
) {
    /**
     * JSON 문자열로 변환
     */
    fun toJson(): String {
        val json = buildMap {
            put("requestId", requestId)
            put("status", status)
            transactionId?.let { put("transactionId", it) }
            message?.let { put("message", it) }
            if (data.isNotEmpty()) {
                put("data", data)
            }
        }
        return JSONObject(json).toString()
    }
    
    companion object {
        /**
         * JSON 문자열에서 PaymentResponse 생성
         */
        fun fromJson(json: String): PaymentResponse {
            val jsonObject = JSONObject(json)
            return PaymentResponse(
                requestId = jsonObject.getString("requestId"),
                status = jsonObject.getString("status"),
                transactionId = jsonObject.optString("transactionId", null),
                message = jsonObject.optString("message", null),
                data = jsonObject.optJSONObject("data")?.let { data ->
                    data.keys().asSequence().associateWith { key -> data.get(key) }
                } ?: emptyMap()
            )
        }
    }
}