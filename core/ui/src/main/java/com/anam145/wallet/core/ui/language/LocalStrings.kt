package com.anam145.wallet.core.ui.language

import androidx.compose.runtime.compositionLocalOf
import com.anam145.wallet.core.common.model.Language

/**
 * 언어에 따른 문자열 리소스를 제공하는 Provider
 * 암시적 데이터 전달 메커니즘
 * Props drilling 없이 컴포넌트 트리 전체에 데이터를 전달할 수 있음
 */
val LocalStrings = compositionLocalOf { Strings() }

/**
 * 언어별 문자열 리소스를 담는 data class
 * 
 * strings.xml 대신 사용하여 실시간 언어 변경을 지원
 */
data class Strings(
    // Header
    val headerTitle: String = "AnamWallet",
    val headerTitleMain: String = "AnamWallet",
    val headerTitleHub: String = "허브",
    val headerTitleBrowser: String = "웹뷰",
    val headerTitleIdentity: String = "디지털 신분증",
    val headerTitleSettings: String = "설정",
    
    // Bottom Navigation
    val navMain: String = "메인",
    val navDid: String = "신분증",
    val navHub: String = "허브",
    val navBrowser: String = "브라우저",
    val navSettings: String = "설정",
    
    // Settings Screen
    val settingsThemeSection: String = "테마",
    val settingsLanguageSection: String = "언어",
    val settingsSupportSection: String = "지원",
    val settingsAboutSection: String = "정보",
    
    val settingsDarkMode: String = "다크 모드",
    val settingsLightMode: String = "라이트 모드",
    val settingsDarkModeDescription: String = "어두운 테마 사용 중",
    val settingsLightModeDescription: String = "밝은 테마 사용 중",
    
    val settingsHelp: String = "도움말",
    val settingsHelpDescription: String = "앱 사용 가이드",
    val settingsFaq: String = "자주 묻는 질문",
    val settingsFaqDescription: String = "FAQ 확인하기",
    
    val settingsAppInfo: String = "앱 소개",
    val settingsAppInfoDescription: String = "AnamWallet에 대해 알아보기",
    val settingsLicense: String = "라이선스",
    val settingsLicenseDescription: String = "오픈소스 라이선스 정보",
    
    val settingsLanguageKorean: String = "한국어",
    val settingsLanguageEnglish: String = "English",
    
    // Feature Screen Messages
    val mainScreenTitle: String = "홈 대시보드",
    val hubScreenTitle: String = "허브 화면",
    val browserScreenTitle: String = "브라우저 화면",
    val identityScreenTitle: String = "디지털 신분증 화면",
    
    // Main Screen
    val mainSectionBlockchain: String = "블록체인",
    val mainSectionApps: String = "앱",
    val mainAddMoreServices: String = "더 많은 서비스 보기",
    val mainNoAppsInstalled: String = "설치된 앱이 없습니다",
    val syncingApps: String = "앱을 준비 중입니다...",
    
    // Common
    val back: String = "뒤로가기",
    val loading: String = "로딩 중...",
    val activated: String = "활성화됨"
)

/**
 * 영어 문자열 리소스
 */
val EnglishStrings = Strings(
    // Header
    headerTitle = "AnamWallet",
    headerTitleMain = "AnamWallet",
    headerTitleHub = "Hub",
    headerTitleBrowser = "WebView",
    headerTitleIdentity = "Digital ID",
    headerTitleSettings = "Settings",
    
    // Bottom Navigation
    navMain = "Main",
    navDid = "ID",
    navHub = "Hub",
    navBrowser = "Browser",
    navSettings = "Settings",
    
    // Settings Screen
    settingsThemeSection = "Theme",
    settingsLanguageSection = "Language",
    settingsSupportSection = "Support",
    settingsAboutSection = "About",
    
    settingsDarkMode = "Dark Mode",
    settingsLightMode = "Light Mode",
    settingsDarkModeDescription = "Using dark theme",
    settingsLightModeDescription = "Using light theme",
    
    settingsHelp = "Help",
    settingsHelpDescription = "App usage guide",
    settingsFaq = "FAQ",
    settingsFaqDescription = "Check frequently asked questions",
    
    settingsAppInfo = "App Info",
    settingsAppInfoDescription = "Learn about AnamWallet",
    settingsLicense = "License",
    settingsLicenseDescription = "Open source license information",
    
    settingsLanguageKorean = "한국어",
    settingsLanguageEnglish = "English",
    
    // Feature Screen Messages
    mainScreenTitle = "Home Dashboard",
    hubScreenTitle = "Hub Screen",
    browserScreenTitle = "Browser Screen",
    identityScreenTitle = "Digital ID Screen",
    
    // Main Screen
    mainSectionBlockchain = "Blockchain",
    mainSectionApps = "Apps",
    mainAddMoreServices = "View more services",
    mainNoAppsInstalled = "No apps installed",
    syncingApps = "Preparing apps...",
    
    // Common
    back = "Back",
    loading = "Loading...",
    activated = "Activated"
)

/**
 * 언어에 따른 Strings 객체 반환
 */
fun getStringsForLanguage(language: Language): Strings {
    return when (language) {
        Language.KOREAN -> Strings()
        Language.ENGLISH -> EnglishStrings
    }
}