package com.anam145.wallet.feature.settings.navigation

/**
 * Settings 기능 내부의 네비게이션 경로 정의
 * 
 * Settings 탭 내에서 사용되는 모든 화면의 경로를 관리합니다.
 */
sealed class SettingsRoute(val route: String) {
    
    /** 메인 설정 화면 */
    data object Main : SettingsRoute("main")
    
    /** 도움말 화면 */
    data object Help : SettingsRoute("help")
    
    /** FAQ 화면 */
    data object FAQ : SettingsRoute("faq")
    
    /** 앱 정보 화면 */
    data object AppInfo : SettingsRoute("app_info")
    
    /** 라이선스 화면 */
    data object License : SettingsRoute("license")
    
    companion object {
        /**
         * route 문자열로 해당하는 SettingsRoute 객체를 찾습니다.
         */
        fun fromRoute(route: String?): SettingsRoute? {
            return when (route) {
                Main.route -> Main
                Help.route -> Help
                FAQ.route -> FAQ
                AppInfo.route -> AppInfo
                License.route -> License
                else -> null
            }
        }
    }
}