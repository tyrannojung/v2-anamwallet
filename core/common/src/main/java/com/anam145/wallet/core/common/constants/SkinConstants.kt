package com.anam145.wallet.core.common.constants

import com.anam145.wallet.core.common.model.Skin

/**
 * 메인 화면 섹션 순서
 */
enum class SectionOrder {
    BLOCKCHAIN_FIRST,  // 블록체인이 위, 앱이 아래 (기본)
    APPS_FIRST        // 앱이 위, 블록체인이 아래
}

/**
 * 스킨별 기본 미니앱 설정
 * 
 * 최초 실행 시 이 값들이 DataStore로 복사되며,
 * 이후에는 DataStore에서 동적으로 관리됩니다.
 */
object SkinConstants {
    
    /**
     * 스킨별 기본 미니앱 ID 목록
     * 
     * 최초 실행 시에만 사용되며, 이후에는 DataStore에서 관리
     */
    val DEFAULT_SKIN_MINIAPPS = mapOf(
        Skin.ANAM to listOf(
            // 빈 상태 테스트를 위해 임시로 비워둠
        ),
        Skin.BUSAN to listOf(
            "com.anam.bitcoin",
            "com.busan.bonmedia",
            "com.busan.card",
            "com.busan.holdem",
            "com.busan.ilbo"
        ),
        Skin.SEOUL to listOf(
            "com.anam.ethereum",
            "com.seoul.bike",
            "com.seoul.culture",
            "com.seoul.news",
            "com.seoul.sto",
            "com.seoul.visitseoul"
        ),
        Skin.LA to listOf(
            "com.anam.bitcoin",
            "com.anam.ethereum",
        )
    )
    
    /**
     * 기본 스킨
     */
    val DEFAULT_SKIN = Skin.ANAM
    
    /**
     * 스킨별 섹션 순서 설정
     * 
     * 부산만 앱이 먼저, 나머지는 블록체인이 먼저
     */
    val DEFAULT_SECTION_ORDERS = mapOf(
        Skin.ANAM to SectionOrder.BLOCKCHAIN_FIRST,
        Skin.BUSAN to SectionOrder.APPS_FIRST,
        Skin.SEOUL to SectionOrder.BLOCKCHAIN_FIRST,
        Skin.LA to SectionOrder.BLOCKCHAIN_FIRST
    )
}