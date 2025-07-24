package com.anam145.wallet.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.anam145.wallet.core.common.model.Skin

/**
 * ANAM Wallet 테마 정의
 * 
 * Material Design 3 기반의 라이트 테마 시스템입니다.
 */


// ========== 라이트 모드 컬러 스킴 ==========
/**
 * 라이트 모드에서 사용되는 색상 팔레트
 * Material 3의 lightColorScheme을 기반으로 ANAM 브랜드 색상으로 커스터마이징
 */
private val AnamLightColorScheme = lightColorScheme(
    // 주요 색상
    primary = AnamPrimary,                    // 주요 액션 (버튼, 링크 등)
    secondary = AnamSecondary,                // 보조 액션
    tertiary = AnamAqua,                      // 강조 색상
    
    // 배경 및 표면
    background = AnamLight,                   // 앱 전체 배경
    surface = AnamLightSurface,               // 카드, 시트 등의 표면
    surfaceVariant = AnamLightBackground,     // 변형된 표면 (예: 선택된 항목)
    
    // 컨테이너 색상
    primaryContainer = AnamLightSurface,      // Primary 색상의 컨테이너
    onPrimaryContainer = AnamTextDark,        // Primary 컨테이너 위의 콘텐츠
    
    // 콘텐츠 색상 (on으로 시작하는 색상들)
    onPrimary = AnamWhite,                    // Primary 색상 위의 콘텐츠
    onSecondary = AnamTextDark,               // Secondary 색상 위의 콘텐츠
    onTertiary = AnamTextDark,                // Tertiary 색상 위의 콘텐츠
    onBackground = AnamTextDark,              // 배경 위의 콘텐츠
    onSurface = AnamTextDark,                 // 표면 위의 콘텐츠
    onSurfaceVariant = AnamTextSecondary,     // 변형된 표면 위의 콘텐츠
    
    // 기타
    outline = AnamLightBorder,                // 아웃라인, 구분선
    error = AnamError,                        // 에러 상태
    onError = AnamWhite                       // 에러 색상 위의 콘텐츠
)

// ========== 메인 테마 컴포저블 ==========
/**
 * ANAM Wallet의 메인 테마 컴포저블
 * 
 * @param skin 적용할 스킨 (기본값: ANAM)
 * @param content 테마가 적용될 컨텐츠
 */
@Composable
fun AnamWalletTheme(
    skin: Skin = Skin.ANAM,
    content: @Composable () -> Unit
) {
    // 스킨에 따른 색상 스킴 선택
    val colorScheme = getColorSchemeForSkin(skin)
    
    // 상태바 색상 설정 (프리뷰 모드가 아닐 때만)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            // Activity 컨텍스트인 경우에만 상태바 설정
            if (context is Activity) {
                val window = context.window
                // 상태바 색상을 배경색과 동일하게 설정
                window.statusBarColor = colorScheme.background.toArgb()
                // 상태바 아이콘 색상 설정 (라이트 모드이므로 어두운 아이콘)
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    // 테마 적용
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}