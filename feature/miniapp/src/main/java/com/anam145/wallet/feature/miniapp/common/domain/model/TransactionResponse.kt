package com.anam145.wallet.feature.miniapp.common.domain.model

import org.json.JSONObject

/**
 * 트랜잭션 응답 도메인 모델
 * 
 * @property requestId 요청 ID
 * @property status 트랜잭션 상태 (success, failed, pending)
 * @property responseData 응답 데이터 (원본 JSON 문자열)
 */
data class TransactionResponse(
    val requestId: String,
    val status: String,
    val responseData: String
) {
    /**
     * JSON 문자열로 변환
     * 
     * MainBridgeService에서 사용하기 위한 래핑된 JSON 생성
     */
    fun toJson(): String {
        return JSONObject().apply {
            put("requestId", requestId)
            put("status", status)
            put("responseData", responseData)
        }.toString()
    }
    
    companion object {
        /**
         * JSON 문자열에서 TransactionResponse 생성
         */
        fun fromJson(json: String): TransactionResponse {
            val jsonObject = JSONObject(json)
            return TransactionResponse(
                requestId = jsonObject.getString("requestId"),
                status = jsonObject.getString("status"),
                responseData = jsonObject.optString("responseData", "{}")
            )
        }
    }
}