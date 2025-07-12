package com.anam145.wallet.core.security.model

/**
 * 지갑 인증 정보를 담는 데이터 클래스
 */
data class Credentials(
    val address: String,
    val privateKey: String
)