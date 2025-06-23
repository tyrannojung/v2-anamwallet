package com.anam145.wallet.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.anam145.wallet.core.ui.R

/**
 * ANAM Wallet 타이포그래피 정의
 * 
 * 헤드라인에는 Cocogoose 폰트를 사용하고,
 * 본문에는 시스템 기본 폰트를 사용합니다.
 */

// ========== 폰트 패밀리 정의 ==========

/**
 * Cocogoose 폰트 패밀리
 * 브랜드 아이덴티티를 위한 커스텀 폰트
 * 주로 헤드라인과 타이틀에 사용
 */
val CocogooseFamily = FontFamily(
    Font(R.font.cocogoose_regular, FontWeight.Normal)
)

// ========== Typography 정의 ==========

/**
 * ANAM Wallet의 Material 3 Typography 설정
 * 
 * Material Design 3의 타입 스케일을 기반으로 하되,
 * ANAM Wallet의 디자인 가이드에 맞게 커스터마이징
 */
val Typography = Typography(
    // Display - 가장 큰 타이틀 (현재는 기본값 사용)
    // displayLarge = TextStyle(...),
    // displayMedium = TextStyle(...),
    // displaySmall = TextStyle(...),
    
    // Headline - 주요 헤드라인
    headlineLarge = TextStyle(
        fontFamily = CocogooseFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    
    headlineMedium = TextStyle(
        fontFamily = CocogooseFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    headlineSmall = TextStyle(
        fontFamily = CocogooseFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // Title - 섹션 타이틀
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body - 본문 텍스트
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label - 버튼, 태그 등의 라벨
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)