package com.anam145.wallet.core.common.constants

import com.anam145.wallet.core.common.model.Skin

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
            "com.anam.bitcoin",
            "com.anam.ethereum",
            "kr.go.government24",
        ),
        Skin.BUSAN to listOf(
            "com.anam.bitcoin",
            "com.anam.ethereum",
            "com.busan.bonmedia",
            "com.busan.busancard",
            "com.busan.busanholdom",
            "com.busan.busanIlbo"
        ),
        Skin.SEOUL to listOf(
            "com.anam.bitcoin",
            "com.anam.ethereum",
            "kr.go.government24"
        ),
        Skin.LA to listOf(
            "com.anam.bitcoin",
            "com.anam.ethereum",
            "kr.go.government24"
        )
    )
    
    /**
     * 기본 스킨
     */
    val DEFAULT_SKIN = Skin.ANAM
    
    /**
     * 현재 활성화된 스킨 목록
     */
    val ENABLED_SKINS = listOf(
        Skin.ANAM,
        Skin.BUSAN
    )
    
    /**
     * 스킨이 활성화되어 있는지 확인
     */
    fun isSkinEnabled(skin: Skin): Boolean {
        return ENABLED_SKINS.contains(skin)
    }
}