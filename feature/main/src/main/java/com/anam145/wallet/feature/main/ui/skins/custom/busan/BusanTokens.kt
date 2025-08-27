package com.anam145.wallet.feature.main.ui.skins.custom.busan

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * 부산 월렛 디자인 토큰
 * 
 * 부산의 시그니처 색상과 테마를 정의합니다.
 */
@Stable
data class BusanTokens(
    // 부산 시그니처 색상
    val busanBlue: Color = Color(0xFF0F2A48),      // 진한 남색 (메인)
    val busanSkyBlue: Color = Color(0xFF5AABDB),   // 하늘색 (포인트)
    val busanLightBlue: Color = Color(0xFFACCDEC), // 연한 하늘색
    val busanPaleBlue: Color = Color(0xFFAECDEC),  // 연한 민트색 (배경)
    
    // 기본 색상
    val blue: Color = Color(0xFF0F539E),
    val black: Color = Color(0xFF0E0E0E),
    val gray: Color = Color(0xFF8F9295),
    val lightBlue: Color = Color(0xFFE6F4FF),
    val white: Color = Color(0xFFFAFCFF),
    
    // 상태 색상
    val green: Color = Color(0xFF4CAF50),
    val red: Color = Color(0xFFF44336),
    
    // 블록체인 색상
    val bitcoin: Color = Color(0xFFF7931A),
    val ethereum: Color = Color(0xFF627EEA),
    val solana: Color = Color(0xFF14F195)
)