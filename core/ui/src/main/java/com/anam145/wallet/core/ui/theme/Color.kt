package com.anam145.wallet.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ANAM Wallet 색상 정의
 * 
 * 색상 체계는 Sui 블록체인의 디자인 시스템을 참고하여
 * 바다(Sea)와 관련된 네이밍을 사용합니다.
 */

// ========== 브랜드 컬러 ==========
// ANAM의 주요 브랜드 색상들

/** 메인 블루 - 주요 액션, 버튼, 링크 등에 사용 */
val AnamSea = Color(0xFF4DA2FF)

/** 연한 하늘색 - 보조 색상, 하이라이트 등에 사용 */
val AnamAqua = Color(0xFFC0E6FF)

// ========== 라이트 모드 컬러 ==========
// 밝은 테마에서 사용되는 색상들

/** 살짝 푸른빛이 도는 화이트 - 라이트 모드 기본 배경 */
val AnamLight = Color(0xFFF8FBFF)

/** 밝은 회색 - 카드나 섹션의 배경색 */
val AnamLightBackground = Color(0xFFF0F4F8)

/** 순수 화이트 - 카드, 다이얼로그 등의 표면 색상 */
val AnamLightSurface = Color(0xFFFFFFFF)

/** 연한 보더 - 구분선, 아웃라인 등에 사용 */
val AnamLightBorder = Color(0xFFE1E8ED)


// ========== 공통 컬러 (시맨틱 컬러) ==========
// 의미를 가진 색상들 - 상태나 피드백을 나타냄

/** 프라이머리 색상 - 주요 액션, CTA 버튼 */
val AnamPrimary = Color(0xFF4DA2FF)

/** 세컨더리 색상 - 보조 액션, 덜 중요한 요소 */
val AnamSecondary = Color(0xFFC0E6FF)

/** 성공 상태 - 완료, 성공 메시지 등 */
val AnamSuccess = Color(0xFF10B981)

/** 에러 상태 - 오류, 경고, 삭제 등 */
val AnamError = Color(0xFFEF4444)

/** 경고 상태 - 주의가 필요한 정보 */
val AnamWarning = Color(0xFFF59E0B)


// ========== 텍스트 컬러 ==========
// 다양한 상황에서 사용되는 텍스트 색상

/** 다크 텍스트 - 라이트 모드에서 주로 사용 */
val AnamTextDark = Color(0xFF0F172A)

/** 보조 텍스트 - 덜 중요한 정보, 힌트 등 */
val AnamTextSecondary = Color(0xFF64748B)


// ========== 레거시 컬러 ==========
// 기존 코드와의 호환성을 위해 유지하는 색상들
// 새 코드에서는 위의 시맨틱한 이름을 사용하는 것을 권장

val AnamBlue = AnamPrimary
val AnamWhite = Color(0xFFFFFFFF)
val AnamTextGray = Color(0xFF777777)
val AnamLightGray = Color(0xFF999999)