package com.anam145.wallet.core.security.model

/**
 * SCrypt 알고리즘 파라미터
 * 
 * 비밀번호 해싱에 사용되는 SCrypt 파라미터를 저장합니다.
 * DataStore에 JSON 형태로 저장되어 나중에 비밀번호 검증 시 사용됩니다.
 * 
 * @property n CPU/메모리 비용 파라미터 (반복 횟수)
 * @property r 블록 크기 파라미터
 * @property p 병렬화 파라미터
 */
data class ScryptParams(
    val n: Int,
    val r: Int,
    val p: Int
)