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
    background = AnamLight,
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
// 바다와 하늘을 연상시키는 블루 계열
private val BusanColorScheme = lightColorScheme(
    primary = Color(0xFF00A6FB),           // 바다 블루
    secondary = Color(0xFFB4E4FF),         // 연한 바다색
    tertiary = Color(0xFF0077CC),          // 진한 바다색
    background = Color(0xFFF0F9FF),        // 밝은 하늘색
    surface = Color(0xFFFFFFFF),           // 깨끗한 흰색
    surfaceVariant = Color(0xFFE6F4FF),    // 매우 연한 하늘색
    primaryContainer = Color(0xFFCCE9FF),   // 연한 Primary
    onPrimaryContainer = Color(0xFF001A33), // 진한 파랑 텍스트
    onPrimary = Color(0xFFFFFFFF),         // 흰색 텍스트
    onSecondary = Color(0xFF003D66),       // 진한 파란 텍스트
    onTertiary = Color(0xFFFFFFFF),        // 흰색 텍스트
    onBackground = Color(0xFF001A33),      // 거의 검은 파랑
    onSurface = Color(0xFF001A33),         // 거의 검은 파랑
    onSurfaceVariant = Color(0xFF4D6B80),  // 중간 톤 파랑
    outline = Color(0xFFB3D9FF),           // 연한 파란 테두리
    error = Color(0xFFEF4444),             // 공통 에러 색상
    onError = Color(0xFFFFFFFF)            // 흰색 텍스트
)

// ========== SEOUL (수도) ==========
// 경복궁 단청의 전통색을 현대적으로 재해석
private val SeoulColorScheme = lightColorScheme(
    primary = Color(0xFFE84545),           // 경복궁 단청 빨강
    secondary = Color(0xFFFFB84D),         // 전통 노랑
    tertiary = Color(0xFF2E7D32),          // 전통 녹색
    background = Color(0xFFFFF8F0),        // 따뜻한 배경
    surface = Color(0xFFFFFFFF),           // 깨끗한 흰색
    surfaceVariant = Color(0xFFFFF0E6),    // 매우 연한 주황
    primaryContainer = Color(0xFFFFCCCC),   // 연한 빨강
    onPrimaryContainer = Color(0xFF330000), // 진한 빨강 텍스트
    onPrimary = Color(0xFFFFFFFF),         // 흰색 텍스트
    onSecondary = Color(0xFF663300),       // 진한 갈색 텍스트
    onTertiary = Color(0xFFFFFFFF),        // 흰색 텍스트
    onBackground = Color(0xFF330000),      // 거의 검은 빨강
    onSurface = Color(0xFF330000),         // 거의 검은 빨강
    onSurfaceVariant = Color(0xFF804040),  // 중간 톤 빨강
    outline = Color(0xFFFFD4A3),           // 연한 주황 테두리
    error = Color(0xFFEF4444),             // 공통 에러 색상
    onError = Color(0xFFFFFFFF)            // 흰색 텍스트
)

// ========== LA (할리우드/서부) ==========
// 할리우드의 화려함과 캘리포니아 선셋을 연상시키는 퍼플/오렌지 계열
private val LAColorScheme = lightColorScheme(
    primary = Color(0xFF9C27B0),           // 할리우드 퍼플
    secondary = Color(0xFFFF6D00),         // 캘리포니아 선셋 오렌지
    tertiary = Color(0xFFFFD600),          // 골든 아워 옐로우
    background = Color(0xFFFFF8F5),        // 따뜻한 크림색 배경
    surface = Color(0xFFFFFFFF),           // 깨끗한 흰색
    surfaceVariant = Color(0xFFF8E6FF),    // 매우 연한 라벤더
    primaryContainer = Color(0xFFE8C4F0),   // 연한 퍼플
    onPrimaryContainer = Color(0xFF2E0A3C), // 진한 퍼플 텍스트
    onPrimary = Color(0xFFFFFFFF),         // 흰색 텍스트
    onSecondary = Color(0xFFFFFFFF),       // 흰색 텍스트
    onTertiary = Color(0xFF3E2E00),        // 진한 갈색 텍스트
    onBackground = Color(0xFF2E0A3C),      // 진한 퍼플
    onSurface = Color(0xFF2E0A3C),         // 진한 퍼플
    onSurfaceVariant = Color(0xFF664270),  // 중간 톤 퍼플
    outline = Color(0xFFDCB8E5),           // 연한 퍼플 테두리
    error = Color(0xFFEF4444),             // 공통 에러 색상
    onError = Color(0xFFFFFFFF)            // 흰색 텍스트
)

/**
 * 스킨에 따른 ColorScheme 반환
 */
fun getColorSchemeForSkin(skin: Skin) = when (skin) {
    Skin.ANAM -> AnamColorScheme
    Skin.BUSAN -> BusanColorScheme
    Skin.SEOUL -> SeoulColorScheme
    Skin.LA -> LAColorScheme
}