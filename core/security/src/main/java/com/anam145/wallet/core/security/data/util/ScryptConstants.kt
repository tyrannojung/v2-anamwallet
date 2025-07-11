package com.anam145.wallet.core.security.data.util

/**
 * SCrypt 알고리즘 공통 상수
 * 
 * 앱 전체에서 일관된 SCrypt 파라미터를 사용하기 위한 상수 정의
 */
object ScryptConstants {
    /**
     * SCrypt N 파라미터 (CPU/메모리 비용)
     * - 모바일 최적화: 16384 (2^14)
     * - 데스크톱: 262144 (2^18) 사용 가능
     */
    const val N = 16384  // 2^14
    
    /**
     * SCrypt N 파라미터 (빠른 버전)
     * - 테스트나 빠른 처리가 필요한 경우 사용
     */
    const val N_LIGHT = 4096  // 2^12
    
    /**
     * SCrypt r 파라미터 (블록 크기)
     * - 메모리 접근 패턴에 영향
     */
    const val R = 8
    
    /**
     * SCrypt p 파라미터 (병렬화 정도)
     * - 모바일에서는 보통 1 사용
     */
    const val P = 1
    
    /**
     * 파생키 길이 (바이트)
     * - 256비트 키 생성
     */
    const val DKLEN = 32
}