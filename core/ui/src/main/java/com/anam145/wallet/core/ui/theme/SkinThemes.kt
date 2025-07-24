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
    primary = Color(0xFF1565C0),           // 부산 오리지널 블루 (깊은 바다)
    secondary = Color(0xFF4FC3F7),         // 부산 글로벌 라이트 블루 (얕은 바다)
    tertiary = Color(0xFF00ACC1),          // 청록색 (항구의 물빛)
    background = Color(0xFFF5FAFE),        // 매우 연한 하늘색
    surface = Color(0xFFFFFFFF),           
    surfaceVariant = Color(0xFFE3F2FD),    // 연한 푸른 회색
    primaryContainer = Color(0xFFBBDEFB),   // 밝은 하늘색
    onPrimaryContainer = Color(0xFF0D47A1), // 진한 남색
    onPrimary = Color(0xFFFFFFFF),         
    onSecondary = Color(0xFF01579B),       // 진한 청색
    onTertiary = Color(0xFFFFFFFF),        
    onBackground = Color(0xFF0D47A1),      
    onSurface = Color(0xFF0D47A1),         
    onSurfaceVariant = Color(0xFF546E7A),  // 청회색
    outline = Color(0xFF90CAF9),           // 연한 청색 테두리
    error = Color(0xFFEF4444),             
    onError = Color(0xFFFFFFFF)            
)

// ========== SEOUL (수도) ==========
// 서울의 현대적이고 역동적인 이미지를 표현
private val SeoulColorScheme = lightColorScheme(
    primary = Color(0xFF00A968),           // 서울 그린 (자연/남산)
    secondary = Color(0xFFE60012),         // 서울 레드 (열정)
    tertiary = Color(0xFF0066CC),          // 서울 블루 (한강)
    background = Color(0xFFFAFAFA),        // 밝고 깨끗한 배경
    surface = Color(0xFFFFFFFF),           
    surfaceVariant = Color(0xFFF5F5F5),    // 모던한 회색조
    primaryContainer = Color(0xFFE8DCC6),   // 웜 베이지
    onPrimaryContainer = Color(0xFF410002), // 진한 레드
    onPrimary = Color(0xFFFFFFFF),         
    onSecondary = Color(0xFFFFFFFF),       
    onTertiary = Color(0xFFFFFFFF),        
    onBackground = Color(0xFF1C1B1F),      // 모던한 다크 그레이
    onSurface = Color(0xFF1C1B1F),         
    onSurfaceVariant = Color(0xFF605D62),  // 중간 그레이
    outline = Color(0xFFE0E0E0),           // 깔끔한 회색 테두리
    error = Color(0xFFBA1A1A),             
    onError = Color(0xFFFFFFFF)            
)

// ========== LA (할리우드) ==========
// 할리우드와 캘리포니아의 따뜻한 햇살을 표현
private val LAColorScheme = lightColorScheme(
    primary = Color(0xFFD4A574),           // 부드러운 탠 (California Tan)
    secondary = Color(0xFFF4A460),         // 샌디 브라운 (캘리포니아 모래)
    tertiary = Color(0xFF87CEEB),          // 스카이 블루 (LA의 맑은 하늘)
    background = Color(0xFFFFFAF0),        // 플로럴 화이트
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFF8DC),    // 콘실크 (따뜻한 크림색)
    primaryContainer = Color(0xFFFFE4B5),   // 모카신
    onPrimaryContainer = Color(0xFF5C4033), // 다크 브라운
    onPrimary = Color(0xFF5C4033),         // 다크 브라운
    onSecondary = Color(0xFF5C4033),       // 다크 브라운
    onTertiary = Color(0xFFFFFFFF),        // 흰색
    onBackground = Color(0xFF3E2723),      // 진한 브라운
    onSurface = Color(0xFF3E2723),         // 진한 브라운
    onSurfaceVariant = Color(0xFF795548),  // 중간 브라운
    outline = Color(0xFFDEB887),           // 버리우드 (연한 브라운)
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