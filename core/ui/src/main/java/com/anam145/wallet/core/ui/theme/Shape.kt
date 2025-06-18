package com.anam145.wallet.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * ANAM Wallet에서 사용되는 Shape 정의
 * 
 * 일관된 모서리 둥글기를 위한 Shape 상수들
 */

// ========== 기본 Shape 정의 ==========

/** 작은 컴포넌트용 둥근 모서리 (4dp) - 칩, 태그 등 */
val ShapeSmall = RoundedCornerShape(4.dp)

/** 중간 컴포넌트용 둥근 모서리 (8dp) - 버튼, 입력 필드 등 */
val ShapeMedium = RoundedCornerShape(8.dp)

/** 일반 카드용 둥근 모서리 (12dp) - 카드, 다이얼로그 등 */
val ShapeNormal = RoundedCornerShape(12.dp)

/** 큰 컴포넌트용 둥근 모서리 (16dp) - 설정 카드, 큰 컨테이너 등 */
val ShapeLarge = RoundedCornerShape(16.dp)

/** 매우 큰 컴포넌트용 둥근 모서리 (24dp) - 바텀시트, 모달 등 */
val ShapeExtraLarge = RoundedCornerShape(24.dp)

// ========== 특수 Shape 정의 ==========

/** 상단만 둥근 모서리 - 바텀시트, 탭 등 */
val ShapeTopRounded = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

/** 하단만 둥근 모서리 - 특정 UI 요소용 */
val ShapeBottomRounded = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 4.dp,
    bottomEnd = 4.dp
)

/** 원형 - 아바타, FAB 등 */
val ShapeCircle = RoundedCornerShape(50)

/** 둥근 버튼용 Shape */
val ShapeButton = RoundedCornerShape(24.dp)