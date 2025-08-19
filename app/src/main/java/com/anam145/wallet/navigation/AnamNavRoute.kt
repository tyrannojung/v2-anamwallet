package com.anam145.wallet.navigation


/**
 * ANAM Wallet의 네비게이션 경로 정의
 * 
 * 앱 내의 모든 화면에 대한 경로를 sealed class로 관리합니다.
 * 이를 통해 타입 안전성을 보장하고 잘못된 경로 사용을 방지합니다.
 * 
 * ## Sealed Class란?
 * - 제한된 계층 구조를 가진 클래스
 * - 모든 하위 클래스가 컴파일 시점에 알려짐
 * - when 표현식에서 else 브랜치 불필요
 * 
 * ## 장점:
 * 1. 타입 안전성 - 오타로 인한 런타임 에러 방지
 * 2. IDE 자동완성 지원
 * 3. 리팩토링 시 모든 사용처 자동 변경
 * 
 * ## 사용 예시:
 * // ✅ 타입 안전한 네비게이션
 * navController.navigate(AnamNavRoute.Main.route)  // 자동완성 지원
 * 
 * // ✅ 안전한 파라미터 처리
 * navController.navigate(
 *     AnamNavRoute.MiniAppDetail.createRoute(appId)  // "miniapp/123"
 * )
 * 
 * // ❌ 기존 방식의 문제점
 * navController.navigate("mian")  // 오타! 런타임 에러 발생
 * navController.navigate("miniapp/" + appId)  // 수동 문자열 조합
 */
sealed class AnamNavRoute(
    val route: String
) {
    
    // ========== Bottom Navigation 화면들 ==========
    // 하단 네비게이션 바에 표시되는 주요 화면들
    
    /** 메인 화면 - 홈 대시보드
     * Kotlin에서 object는 싱글톤(singleton)을 간편하게 만드는 키워드,
     * 이 객체는 앱 전체에서 단 하나의 인스턴스만 존재
     * 경로는 상태가 없는 불변 값 이므로, 여러 번 인스턴스를 생성할 필요 없이 하나만 있으면 충분
     * 아래는 sealed class AnamNavRoute(val route: String)를 상속 받는 하위 클래스들
     * */
    data object Main : AnamNavRoute("main")
    
    /** 허브 화면 - 미니앱 및 서비스 목록 */
    data object Hub : AnamNavRoute("hub")
    
    /** 브라우저 화면 - 웹 브라우징 */
    data object Browser : AnamNavRoute("browser")
    
    // DID 기능 임시 비활성화
    // /** 신원(Identity) 화면 - DID/VC 관리 */
    // data object Identity : AnamNavRoute("identity")
    
    /** 설정 화면 - 앱 설정 및 프로필 */
    data object Settings : AnamNavRoute("settings")
    
    // ========== 상세 화면들 ==========
    // 특정 아이템이나 기능의 상세 화면들
    
    /** 
    * 미니앱 상세 화면
    * 
    * 사용 예시:
    * // 미니앱 ID가 "weather-app"인 경우
    * navController.navigate(
    *     AnamNavRoute.MiniAppDetail.createRoute("weather-app")
    * )
    * // 결과: "miniapp/weather-app"
    */
    data object MiniAppDetail : AnamNavRoute("miniapp/{appId}") {
        /** 특정 미니앱으로 이동하기 위한 경로 생성 */
        fun createRoute(appId: String) = "miniapp/$appId"
    }
    
    /** 모듈 상세 화면 (블록체인 모듈 등) */
    data object ModuleDetail : AnamNavRoute("module/{moduleId}") {
        /** 특정 모듈로 이동하기 위한 경로 생성 */
        fun createRoute(moduleId: String) = "module/$moduleId"
    }
    
    /** 학생증 상세 화면 */
    data object StudentCardDetail : AnamNavRoute("student_card")
    
    /** 운전면허증 상세 화면 */
    data object DriverLicenseDetail : AnamNavRoute("driver_license")
    
    // ========== 인증 화면들 ==========
    // 로그인 및 비밀번호 설정 화면들
    
    /** 로그인 화면 - 비밀번호 입력 */
    data object Login : AnamNavRoute("auth/login")
    
    /** 비밀번호 설정 화면 - 최초 설정 */
    data object SetupPassword : AnamNavRoute("auth/setup")
    
    // ========== 설정 관련 화면들 (Deprecated - SettingsRoute 사용) ==========
    // Settings는 이제 Nested Navigation을 사용하므로
    // 이 route들은 더 이상 메인 NavHost에서 사용되지 않습니다.
    // 호환성을 위해 남겨두었지만, SettingsRoute를 사용하세요.
    
    @Deprecated("Use SettingsRoute.Help instead", level = DeprecationLevel.WARNING)
    data object Help : AnamNavRoute("settings/help")
    
    @Deprecated("Use SettingsRoute.FAQ instead", level = DeprecationLevel.WARNING)
    data object FAQ : AnamNavRoute("settings/faq")
    
    @Deprecated("Use SettingsRoute.AppInfo instead", level = DeprecationLevel.WARNING)
    data object AppInfo : AnamNavRoute("settings/appinfo")
    
    @Deprecated("Use SettingsRoute.License instead", level = DeprecationLevel.WARNING)
    data object License : AnamNavRoute("settings/license")

    
    companion object {
        /**
         * 주어진 route 문자열로 해당하는 AnamNavRoute 객체를 찾습니다.
         * 
         * @param route 네비게이션 route 문자열
         * @return 해당하는 AnamNavRoute 객체, 없으면 null
         */
        fun fromRoute(route: String?): AnamNavRoute? {
            return when (route) {
                Main.route -> Main
                Hub.route -> Hub
                Browser.route -> Browser
                // Identity.route -> Identity  // DID 기능 임시 비활성화
                Settings.route -> Settings
                StudentCardDetail.route -> StudentCardDetail
                DriverLicenseDetail.route -> DriverLicenseDetail
                Login.route -> Login
                SetupPassword.route -> SetupPassword
                Help.route -> Help
                FAQ.route -> FAQ
                AppInfo.route -> AppInfo
                License.route -> License
                else -> {
                    // 파라미터가 있는 route 처리
                    when {
                        route?.startsWith("miniapp/") == true -> MiniAppDetail
                        route?.startsWith("module/") == true -> ModuleDetail
                        else -> null
                    }
                }
            }
        }
    }
}