package com.anam145.wallet.core.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.anam145.wallet.core.common.model.Skin

/**
 * 스킨별 색상 테마 정의
 * 
 * 각 지역의 특색을 반영한 Material3 색상 스킴을 제공합니다.
 */

// ========== ANAM (기본) ==========
// 기존 ANAM 브랜드 색상 유지
private val AnamColorScheme = lightColorScheme(
    primary = AnamPrimary,
    secondary = AnamSecondary,
    tertiary = AnamAqua,
    background = Color(0xFFFCFCFC),
    surface = AnamLightSurface,
    surfaceVariant = AnamLightBackground,
    primaryContainer = AnamLightSurface,
    onPrimaryContainer = AnamTextDark,
    onPrimary = AnamWhite,
    onSecondary = AnamTextDark,
    onTertiary = AnamTextDark,
    onBackground = AnamTextDark,
    onSurface = AnamTextDark,
    onSurfaceVariant = AnamTextSecondary,
    outline = AnamLightBorder,
    error = AnamError,
    onError = AnamWhite
)

// ========== BUSAN (해양 도시) ==========
// 부산 - 진한 네이비에서 밝은 하늘색 그라데이션
private val BusanColorScheme = lightColorScheme(
    primary = Color(0xFF1D3679),           // 진한 네이비 (깊은 바다)
    secondary = Color(0xFF4686BC),         // 중간 블루
    tertiary = Color(0xFF6CD0FF),          // 밝은 하늘색
    background = Color(0xFFFCFCFC),        // 거의 흰색 (약간의 회색빛)
    surface = Color(0xFFFFFFFF),           
    surfaceVariant = Color(0xFFECF6FF),    // 매우 연한 하늘색
    primaryContainer = Color(0xFFCCE5FF),   // 밝은 하늘색
    onPrimaryContainer = Color(0xFF0E1729), // 진한 네이비
    onPrimary = Color(0xFFFFFFFF),         
    onSecondary = Color(0xFFFFFFFF),       
    onTertiary = Color(0xFFFFFFFF),        
    onBackground = Color(0xFF1C1B1F),      
    onSurface = Color(0xFF1C1B1F),         
    onSurfaceVariant = Color(0xFF3E4A5C),  
    outline = Color(0xFF99C9E8),           
    error = Color(0xFFEF4444),             
    onError = Color(0xFFFFFFFF)            
)


/**
 * 스킨에 따른 ColorScheme 반환
 */
fun getColorSchemeForSkin(skin: Skin) = when (skin) {
    Skin.ANAM -> AnamColorScheme
    Skin.BUSAN -> BusanColorScheme
}