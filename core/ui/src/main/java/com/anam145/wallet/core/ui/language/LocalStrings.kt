package com.anam145.wallet.core.ui.language

import androidx.compose.runtime.compositionLocalOf
import com.anam145.wallet.core.common.model.Language
import com.anam145.wallet.core.common.model.Skin

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
    val headerTitle: String = "Anam Wallet",
    val headerTitleMain: String = "Anam Wallet",
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
    val settingsLanguageSection: String = "언어",
    val settingsSupportSection: String = "지원",
    val settingsAboutSection: String = "정보",
    
    val settingsHelp: String = "도움말",
    val settingsHelpDescription: String = "앱 사용 가이드",
    val settingsFaq: String = "자주 묻는 질문",
    val settingsFaqDescription: String = "FAQ 확인하기",
    
    val settingsAppInfo: String = "앱 소개",
    val settingsAppInfoDescription: String = "Anam Wallet에 대해 알아보기",
    val settingsLicense: String = "라이선스",
    val settingsLicenseDescription: String = "오픈소스 라이선스 정보",
    
    val settingsLanguageKorean: String = "한국어",
    val settingsLanguageEnglish: String = "English",
    
    // Feature Screen Messages
    val mainScreenTitle: String = "홈 대시보드",
    val hubScreenTitle: String = "허브 화면",
    val browserScreenTitle: String = "브라우저",
    val identityScreenTitle: String = "디지털 신분증 화면",
    
    // Browser Screen
    val browserUrlPlaceholder: String = "URL 입력 또는 검색",
    val browserSearchPlaceholder: String = "검색어 또는 주소 입력",
    val cancel: String = "취소",
    val browserSearchDuckDuckGo: String = "DuckDuckGo에서 검색",
    val browserNoBookmarks: String = "북마크가 없습니다",
    val browserNoBookmarksDescription: String = "웹사이트 방문 시 북마크를 추가하세요",
    val browserBookmarkAdded: String = "북마크가 추가되었습니다",
    val browserBookmarkRemoved: String = "북마크가 삭제되었습니다",
    val browserDeleteBookmarkTitle: String = "북마크 삭제",
    val browserDeleteBookmarkMessage: String = "%s을(를) 북마크에서 삭제하시겠습니까?",
    val browserPageLoadError: String = "페이지를 불러올 수 없습니다",
    val browserGoBack: String = "뒤로 가기",
    val browserGoForward: String = "앞으로 가기",
    val browserReload: String = "새로고침",
    val browserHome: String = "홈",
    val browserCancel: String = "취소",
    val browserDelete: String = "삭제",
    
    // Main Screen
    val mainSectionBlockchain: String = "블록체인",
    val mainSectionApps: String = "앱",
    val mainAddMoreServices: String = "더 많은 서비스 보기",
    val mainNoAppsInstalled: String = "설치된 앱이 없습니다",
    val mainNoBlockchainsInstalled: String = "설치된 블록체인이 없습니다",
    val mainEmptyStateTitle: String = "ANAM 월렛에 오신 것을 환영합니다",
    val mainEmptyStateTitleBusan: String = "부산 월렛에 오신 것을 환영합니다",
    val mainEmptyStateDescription: String = "하단의 허브 탭에서 앱과 블록체인을 추가할 수 있습니다",
    val syncingApps: String = "앱을 준비 중입니다...",
    
    // Common
    val back: String = "뒤로가기",
    val loading: String = "로딩 중...",
    val activated: String = "활성화됨",
    val active: String = "활성",
    
    // Auth - Login Screen
    val loginTitle: String = "비밀번호를 입력하여 지갑에 접근하세요",
    val loginPasswordLabel: String = "비밀번호",
    val loginPasswordPlaceholder: String = "최소 8자 이상",
    val loginPasswordHide: String = "비밀번호 숨기기",
    val loginPasswordShow: String = "비밀번호 보기",
    val loginUnlockButton: String = "잠금 해제",
    val loginForgotPassword: String = "비밀번호를 잊으셨나요? 앱을 재설치해야 합니다.",
    val loginPasswordMismatch: String = "비밀번호가 일치하지 않습니다",
    
    // Auth - Setup Password Screen
    val setupPasswordTitle: String = "비밀번호 설정",
    val setupPasswordDescription: String = "지갑을 보호할 비밀번호를 설정하세요.\n이 비밀번호는 앱 접근 시 필요합니다.",
    val setupPasswordLabel: String = "비밀번호",
    val setupPasswordPlaceholder: String = "최소 8자 이상 입력",
    val setupPasswordConfirmLabel: String = "비밀번호 확인",
    val setupPasswordConfirmPlaceholder: String = "비밀번호를 다시 입력하세요",
    val setupPasswordMatch: String = "비밀번호 일치",
    val setupPasswordButton: String = "비밀번호 설정",
    val setupPasswordWarningTitle: String = "주의사항",
    val setupPasswordWarningContent: String = "• 비밀번호를 잊으면 지갑에 접근할 수 없습니다\n• 앱을 재설치하면 모든 데이터가 삭제됩니다",
    val setupPasswordStrengthWeak: String = "약함",
    val setupPasswordStrengthMedium: String = "보통",
    val setupPasswordStrengthStrong: String = "강함",
    
    // Auth - Error Messages
    val authErrorPasswordTooShort: String = "비밀번호는 최소 8자 이상이어야 합니다",
    val authErrorPasswordMismatch: String = "비밀번호가 일치하지 않습니다",
    val authErrorLoginFailed: String = "로그인 중 오류가 발생했습니다",
    val authErrorPasswordSetupFailed: String = "비밀번호 설정 중 오류가 발생했습니다",
    val authErrorDIDCreationFailed: String = "디지털 신원 생성에 실패했습니다. 네트워크 연결을 확인해주세요.",
    
    // Skin
    val settingsSkinSection: String = "스킨",
    val skinAnam: String = "ANAM",
    val skinSeoul: String = "서울",
    val skinBusan: String = "부산",
    val skinLA: String = "LA",
    
    // Service Connection
    val serviceDisconnected: String = "서비스 연결 끊김",
    val retry: String = "재연결",
    val tryAgain: String = "다시 시도",
    
    // Identity (DID) Screen
    val identityMobileDriverLicense: String = "모바일 운전면허증",
    val identityMobileStudentId: String = "모바일 학생증",
    val identityEligibility: String = "발급대상",
    val identityRequirements: String = "준 비 물",
    val identityDriverEligibility: String = "1종, 2종 자동차운전면허 소지자",
    val identityStudentEligibility: String = "고려대학교 재학생",
    val identityNoRequirements: String = "없음(홍길동으로 발급)",
    val identityInquire: String = "조회하기",
    
    // Identity Detail Screens
    val identityRefresh: String = "새로고침",
    val identityPhoto: String = "사진",
    val identityEnlarge: String = "크게보기",
    val identityQRScan: String = "QR을 태하여\n카메라에 스캔하세요.",
    val identityKoreaUniversity: String = "고려대학교",
    val identityGraduateSchool: String = "Student ID",
    val identityFinancialSecurity: String = "금융보안학과",
    val identityBlockchainMajor: String = "블록체인전공",
    val identityRepublicOfKorea: String = "대한민국",
    val identityDriversLicense: String = "자동차운전면허증",
    val identityLicenseType: String = "종류",
    val identityClass1Regular: String = "제1종 보통",
    val identityIssueDate: String = "발급일",
    val identityAptitudeTest: String = "적성검사",
    val identityRenewal: String = "갱신기간",
    val identityMyIds: String = "내 신분증",
    val identityAddId: String = "신분증 추가",
    val identityIssueNewId: String = "발급하기",
    val identityIssueId: String = "신분증 발급",
    val identityValid: String = "유효",
    val identityStudentIdCard: String = "학생증",
    val identityDriverLicenseCard: String = "운전면허증",
    val identityIssueStudentId: String = "홍길동\n고려대 학생증\n발급하기",
    val identityIssueDriverLicense: String = "홍길동\n운전면허증\n발급하기",
    val identityViewDetail: String = "상세보기",
    val identitySampleName: String = "홍길동",
    val identityIssued: String = "발급완료",
    val identityAllIssued: String = "모든 신분증이 발급되었습니다.",
    val identityNoIssuedIds: String = "발급된 모바일 신분증이 없습니다.",
    val dismiss: String = "닫기",
    
    // Help Screen
    val helpTitle: String = "도움말",
    val helpPasswordTitle: String = "앱 비밀번호 설정",
    val helpPasswordContent: String = "ANAM Wallet을 처음 실행하면 앱 보호를 위한 비밀번호 설정이 필요합니다. 최소 8자 이상의 비밀번호를 설정하세요. 이 비밀번호는 앱 접근 시 매번 필요하며, 분실 시 앱을 재설치해야 하므로 안전하게 보관하세요.",
    val helpMiniAppTitle: String = "미니앱 설치 및 관리",
    val helpMiniAppContent: String = "하단 네비게이션의 '허브' 탭에서 다양한 블록체인 지갑과 웹 서비스를 설치할 수 있습니다. 설치하고 싶은 미니앱을 선택하여 '설치' 버튼을 누르면 메인 화면에 추가됩니다. 설치된 미니앱은 메인 화면에서 바로 실행할 수 있습니다.",
    val helpBlockchainTitle: String = "블록체인 지갑 사용",
    val helpBlockchainContent: String = "메인 화면에서 원하는 블록체인 지갑(Bitcoin, Ethereum 등)을 선택하면 해당 지갑으로 이동합니다. 각 지갑에서는 잔액 확인, 송금, 수신 주소 생성 등의 기능을 사용할 수 있습니다. 활성 블록체인은 상단 헤더에 표시되며, 탭하여 빠르게 전환할 수 있습니다.",
    val helpBrowserTitle: String = "웹 브라우저",
    val helpBrowserContent: String = "하단 네비게이션의 '브라우저' 탭을 통해 웹사이트와 DApp에 접근할 수 있습니다. 북마크 기능을 지원하며, 자주 방문하는 사이트를 저장하여 빠르게 접속할 수 있습니다.",
    val helpLanguageTitle: String = "언어 및 스킨 변경",
    val helpLanguageContent: String = "설정' 탭에서 앱의 언어(한국어/English)와 스킨(ANAM/Busan)을 변경할 수 있습니다. 스킨에 따라 기본 설치되는 미니앱과 UI 테마가 달라집니다.",
    
    // FAQ Screen
    val faqTitle: String = "자주 묻는 질문",
    val faqQuestion1: String = "미니앱이란 무엇인가요?",
    val faqAnswer1: String = "미니앱은 ANAM Wallet 내에서 독립적으로 실행되는 작은 애플리케이션입니다. 블록체인 지갑(Bitcoin, Ethereum 등)과 웹 서비스(Government24, 부산카드 등)를 미니앱 형태로 설치하여 사용할 수 있습니다. 각 미니앱은 별도의 프로세스에서 실행되어 보안성과 안정성이 보장됩니다.",
    val faqQuestion2: String = "새로운 블록체인 지갑을 추가하려면 어떻게 하나요?",
    val faqAnswer2: String = "하단 네비게이션의 '허브' 탭에서 원하는 블록체인 미니앱을 찾아 '설치' 버튼을 누르면 됩니다. 설치된 미니앱은 메인 화면에 나타나며, 탭하여 바로 사용할 수 있습니다. 필요 없는 미니앱은 언제든지 제거할 수 있습니다.",
    val faqQuestion3: String = "앱 비밀번호를 잊어버렸어요. 어떻게 해야 하나요?",
    val faqAnswer3: String = "보안상의 이유로 앱 비밀번호는 복구할 수 없습니다. 앱을 삭제하고 재설치해야 하며, 이 경우 모든 데이터가 초기화됩니다. 따라서 비밀번호는 안전한 곳에 별도로 보관하시기 바랍니다.",
    val faqQuestion4: String = "여러 개의 블록체인을 동시에 사용할 수 있나요?",
    val faqAnswer4: String = "네, 가능합니다. ANAM Wallet은 모듈형 구조로 설계되어 여러 블록체인 미니앱을 동시에 설치하고 관리할 수 있습니다. 상단 헤더에서 현재 활성화된 블록체인을 확인하고, 탭하여 빠르게 전환할 수 있습니다.",
    val faqQuestion5: String = "스킨이 무엇이며, 어떻게 변경하나요?",
    val faqAnswer5: String = "스킨은 앱의 테마와 기본 설치 미니앱을 결정합니다. 'ANAM'과 'Busan' 스킨을 제공하며, 설정 화면에서 변경할 수 있습니다. Busan 스킨은 부산 지역 서비스가 기본 설치되고, 앱 섹션이 먼저 표시됩니다.",
    val faqQuestion6: String = "미니앱이 업데이트되면 어떻게 알 수 있나요?",
    val faqAnswer6: String = "허브에서 설치된 미니앱의 업데이트를 확인할 수 있습니다. 새 버전이 있으면 업데이트 버튼이 표시되며, 탭하여 최신 버전으로 업데이트할 수 있습니다.",
    val faqQuestion7: String = "오프라인에서도 사용할 수 있나요?",
    val faqAnswer7: String = "설치된 미니앱은 오프라인에서도 기본 기능을 사용할 수 있습니다. 단, 잔액 조회나 트랜잭션 전송 같은 네트워크가 필요한 기능은 인터넷 연결이 필요합니다.",
    val faqQuestion8: String = "브라우저의 북마크는 어디에 저장되나요?",
    val faqAnswer8: String = "북마크는 앱 내부에 안전하게 저장됩니다. 브라우저 하단의 북마크 아이콘을 탭하여 추가/제거할 수 있으며, 북마크 목록에서 저장된 사이트에 빠르게 접속할 수 있습니다.",
    
    // App Info Screen
    val appInfoTitle: String = "앱 정보",
    val appInfoVersion: String = "버전",
    val appInfoDescription: String = "ANAM Wallet은 사용자 맞춤형 모듈성을 핵심 원칙으로 하는 혁신적인 암호화폐 지갑입니다. 단순한 기능이 아닌 근본 철학으로서, 사용자가 원하는 블록체인을 추가하고 불필요한 것은 제거하여 자신에게 완벽하게 맞는 지갑을 만들 수 있습니다. 더 이상 여러 지갑을 관리하거나 업데이트를 기다릴 필요가 없습니다.\n\n개방성을 위해 설계된 ANAM Wallet은 누구나 친숙한 웹 표준(HTML, CSS, JavaScript)으로 모듈을 만들고 배포할 수 있어 생태계의 빠른 성장을 촉진합니다.\n\n이러한 개방성은 완벽한 보안으로 뒷받침됩니다. 특허 받은 멀티 프로세스 격리 기술로 모든 모듈이 독립된 샌드박스 프로세스와 전용 메모리에서 실행되며, 메인 프로세스가 모든 통신을 중재합니다. 악의적인 모듈도 다른 모듈의 개인키나 데이터에 접근할 수 없습니다.\n\n블록체인을 넘어 ANAM Wallet은 애플리케이션 모듈과 분산 신원(DID)을 지원합니다. 통합 API가 모든 것을 연결하여 어떤 서비스든 블록체인과 상호작용할 수 있습니다. 인증과 결제가 하나의 안전한 인터페이스를 통해 이루어집니다.\n\nANAM Wallet은 Web3를 실용적으로 만들어 블록체인 기술과 실제 서비스를 연결합니다.",
    val appInfoDeveloper: String = "개발자",
    val appInfoDeveloperName: String = "ANAM Labs",
    val appInfoWebsite: String = "웹사이트",
    val appInfoWebsiteUrl: String = "https://anamwallet.com",
    val appInfoContact: String = "문의",
    val appInfoContactEmail: String = "support@anamwallet.com",
    
    // License Screen
    val licenseTitle: String = "오픈소스 라이선스",
    val licenseCopyright: String = "Copyright © 2024 ANAM Labs. All rights reserved.",
    val licenseIntro: String = "이 애플리케이션은 다음의 오픈소스 라이브러리를 사용합니다:",
    val licenseApache: String = "Apache License 2.0",
    val licenseMit: String = "MIT License",
    val licenseViewLicense: String = "라이선스 보기"
)

/**
 * 영어 문자열 리소스
 */
val EnglishStrings = Strings(
    // Header
    headerTitle = "Anam Wallet",
    headerTitleMain = "Anam Wallet",
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
    settingsLanguageSection = "Language",
    settingsSupportSection = "Support",
    settingsAboutSection = "About",
    
    settingsHelp = "Help",
    settingsHelpDescription = "App usage guide",
    settingsFaq = "FAQ",
    settingsFaqDescription = "Check frequently asked questions",
    
    settingsAppInfo = "App Info",
    settingsAppInfoDescription = "Learn about Anam Wallet",
    settingsLicense = "License",
    settingsLicenseDescription = "Open source license information",
    
    settingsLanguageKorean = "한국어",
    settingsLanguageEnglish = "English",
    
    // Feature Screen Messages
    mainScreenTitle = "Home Dashboard",
    hubScreenTitle = "Hub Screen",
    browserScreenTitle = "Browser",
    identityScreenTitle = "Digital ID Screen",
    
    // Browser Screen
    browserUrlPlaceholder = "Enter URL or search",
    browserSearchPlaceholder = "Search or enter address",
    cancel = "Cancel",
    browserSearchDuckDuckGo = "Search on DuckDuckGo",
    browserNoBookmarks = "No bookmarks",
    browserNoBookmarksDescription = "Add bookmarks when visiting websites",
    browserBookmarkAdded = "Bookmark added",
    browserBookmarkRemoved = "Bookmark removed",
    browserDeleteBookmarkTitle = "Delete Bookmark",
    browserDeleteBookmarkMessage = "Delete %s from bookmarks?",
    browserPageLoadError = "Unable to load page",
    browserGoBack = "Go back",
    browserGoForward = "Go forward",
    browserReload = "Reload",
    browserHome = "Home",
    browserCancel = "Cancel",
    browserDelete = "Delete",
    
    // Main Screen
    mainSectionBlockchain = "Blockchain",
    mainSectionApps = "Apps",
    mainAddMoreServices = "View more services",
    mainNoAppsInstalled = "No apps installed",
    mainNoBlockchainsInstalled = "No blockchains installed",
    mainEmptyStateTitle = "Welcome to ANAM Wallet",
    mainEmptyStateTitleBusan = "Welcome to Busan Wallet",
    mainEmptyStateDescription = "You can add apps and blockchains from the Hub tab below",
    syncingApps = "Preparing apps...",
    
    // Common
    back = "Back",
    loading = "Loading...",
    activated = "Activated",
    active = "Active",
    
    // Auth - Login Screen
    loginTitle = "Enter password to access your wallet",
    loginPasswordLabel = "Password",
    loginPasswordPlaceholder = "At least 8 characters",
    loginPasswordHide = "Hide password",
    loginPasswordShow = "Show password",
    loginUnlockButton = "Unlock",
    loginForgotPassword = "Forgot password? You need to reinstall the app.",
    loginPasswordMismatch = "Password does not match",
    
    // Auth - Setup Password Screen
    setupPasswordTitle = "Set Password",
    setupPasswordDescription = "Set a password to protect your wallet.\nThis password will be required to access the app.",
    setupPasswordLabel = "Password",
    setupPasswordPlaceholder = "Enter at least 8 characters",
    setupPasswordConfirmLabel = "Confirm Password",
    setupPasswordConfirmPlaceholder = "Re-enter your password",
    setupPasswordMatch = "Password match",
    setupPasswordButton = "Set Password",
    setupPasswordWarningTitle = "Warning",
    setupPasswordWarningContent = "• If you forget your password, you cannot access your wallet\n• Reinstalling the app will delete all data",
    setupPasswordStrengthWeak = "Weak",
    setupPasswordStrengthMedium = "Medium",
    setupPasswordStrengthStrong = "Strong",
    
    // Auth - Error Messages
    authErrorPasswordTooShort = "Password must be at least 8 characters",
    authErrorPasswordMismatch = "Passwords do not match",
    authErrorLoginFailed = "An error occurred during login",
    authErrorPasswordSetupFailed = "An error occurred while setting password",
    authErrorDIDCreationFailed = "Failed to create digital identity. Please check your network connection.",
    
    // Skin
    settingsSkinSection = "Skin",
    skinAnam = "ANAM",
    skinSeoul = "Seoul",
    skinBusan = "Busan",
    skinLA = "LA",
    
    // Service Connection
    serviceDisconnected = "Service connection lost",
    retry = "Retry",
    tryAgain = "Try again",
    
    // Identity (DID) Screen
    identityMobileDriverLicense = "Mobile Driver's License",
    identityMobileStudentId = "Mobile Student ID",
    identityEligibility = "Eligibility",
    identityRequirements = "Requirements",
    identityDriverEligibility = "Class 1 or 2 driver's license holder",
    identityStudentEligibility = "Korea University student",
    identityNoRequirements = "None (Issued to Hong Gildong)",
    identityInquire = "Inquire",
    
    // Identity Detail Screens
    identityRefresh = "Refresh",
    identityPhoto = "Photo",
    identityEnlarge = "Enlarge",
    identityQRScan = "Scan QR code\nwith camera.",
    identityKoreaUniversity = "Korea University",
    identityGraduateSchool = "Student ID",
    identityFinancialSecurity = "Financial Security",
    identityBlockchainMajor = "Blockchain Major",
    identityRepublicOfKorea = "Republic of Korea",
    identityDriversLicense = "Driver's License",
    identityLicenseType = "Type",
    identityClass1Regular = "Class 1 Regular",
    identityIssueDate = "Issue Date",
    identityAptitudeTest = "Aptitude Test",
    identityRenewal = "Renewal",
    identityMyIds = "My IDs",
    identityAddId = "Add ID",
    identityIssueNewId = "Issue New ID",
    identityIssueId = "Issue New ID",
    identityValid = "Valid",
    identityStudentIdCard = "Student ID",
    identityDriverLicenseCard = "Driver's License",
    identityIssueStudentId = "Issue\nHong Gildong\nKorea Univ. ID",
    identityIssueDriverLicense = "Issue\nHong Gildong\nDriver's License",
    identityViewDetail = "Detail",
    identitySampleName = "Hong Gildong",
    identityIssued = "Issued",
    identityAllIssued = "All IDs have been issued.",
    identityNoIssuedIds = "No digital IDs have been issued yet.",
    dismiss = "Dismiss",
    
    // Help Screen
    helpTitle = "Help",
    helpPasswordTitle = "Setting App Password",
    helpPasswordContent = "When you first launch ANAM Wallet, you need to set a password to protect your app. Set a password with at least 8 characters. This password is required every time you access the app. If lost, you must reinstall the app, so keep it safe.",
    helpMiniAppTitle = "Installing and Managing Mini-Apps",
    helpMiniAppContent = "You can install various blockchain wallets and web services from the 'Hub' tab in the bottom navigation. Select the mini-app you want to install and press the 'Install' button to add it to your main screen. Installed mini-apps can be launched directly from the main screen.",
    helpBlockchainTitle = "Using Blockchain Wallets",
    helpBlockchainContent = "Select your desired blockchain wallet (Bitcoin, Ethereum, etc.) from the main screen to access it. Each wallet provides features like balance checking, sending funds, and generating receiving addresses. The active blockchain is displayed in the header and can be quickly switched by tapping.",
    helpBrowserTitle = "Web Browser",
    helpBrowserContent = "Access websites and DApps through the 'Browser' tab in the bottom navigation. Bookmark functionality is supported, allowing you to save and quickly access frequently visited sites.",
    helpLanguageTitle = "Language and Skin Settings",
    helpLanguageContent = "Change the app's language (Korean/English) and skin (ANAM/Busan) in the 'Settings' tab. Different skins come with different default mini-apps and UI themes.",
    
    // FAQ Screen
    faqTitle = "FAQ",
    faqQuestion1 = "What are mini-apps?",
    faqAnswer1 = "Mini-apps are small applications that run independently within ANAM Wallet. You can install and use blockchain wallets (Bitcoin, Ethereum, etc.) and web services (Government24, Busan Card, etc.) as mini-apps. Each mini-app runs in a separate process, ensuring security and stability.",
    faqQuestion2 = "How do I add a new blockchain wallet?",
    faqAnswer2 = "Go to the 'Hub' tab in the bottom navigation, find the blockchain mini-app you want, and press the 'Install' button. Installed mini-apps appear on the main screen and can be accessed immediately by tapping. You can remove unnecessary mini-apps at any time.",
    faqQuestion3 = "I forgot my app password. What should I do?",
    faqAnswer3 = "For security reasons, the app password cannot be recovered. You must delete and reinstall the app, which will reset all data. Therefore, please keep your password in a safe place.",
    faqQuestion4 = "Can I use multiple blockchains simultaneously?",
    faqAnswer4 = "Yes, you can. ANAM Wallet is designed with a modular architecture that allows you to install and manage multiple blockchain mini-apps simultaneously. You can check the currently active blockchain in the header and quickly switch by tapping.",
    faqQuestion5 = "What are skins and how do I change them?",
    faqAnswer5 = "Skins determine the app's theme and default installed mini-apps. We provide 'ANAM' and 'Busan' skins, which can be changed in the Settings screen. The Busan skin comes with Busan regional services pre-installed and displays the apps section first.",
    faqQuestion6 = "How do I know when mini-apps are updated?",
    faqAnswer6 = "You can check for updates to installed mini-apps in the Hub. If a new version is available, an update button will appear, which you can tap to update to the latest version.",
    faqQuestion7 = "Can I use the app offline?",
    faqAnswer7 = "Installed mini-apps can use basic functions offline. However, features that require network access, such as balance checking or transaction sending, need an internet connection.",
    faqQuestion8 = "Where are browser bookmarks stored?",
    faqAnswer8 = "Bookmarks are securely stored within the app. You can add/remove bookmarks by tapping the bookmark icon at the bottom of the browser, and quickly access saved sites from the bookmark list.",
    
    // App Info Screen
    appInfoTitle = "App Info",
    appInfoVersion = "Version",
    appInfoDescription = "AnamWallet is a revolutionary cryptocurrency wallet built on user-configurable modularity—its core principle, not just a feature. Users create wallets that fit them perfectly, adding the blockchains they want and removing the ones they don't. No more juggling multiple wallets or waiting on vendor updates.\n\nEngineered for openness, AnamWallet lets anyone build and deploy modules with familiar web standards (HTML, CSS, JavaScript), fueling rapid ecosystem growth.\n\nThis openness is reinforced by absolute security: our patented multi-process isolation runs every module in its own sandboxed process with dedicated memory, while the main process mediates all communication. Even a malicious module can't touch other modules' private keys or data.\n\nBeyond blockchain, AnamWallet supports application modules and Decentralized Identifiers (DID). A unified API ties everything together so any service can interact with blockchains. Authentication and payments flow through one secure interface.\n\nAnamWallet makes Web3 practical—bridging blockchain technology with real-world services.",
    appInfoDeveloper = "Developer",
    appInfoDeveloperName = "ANAM Labs",
    appInfoWebsite = "Website",
    appInfoWebsiteUrl = "https://anamwallet.com",
    appInfoContact = "Contact",
    appInfoContactEmail = "support@anamwallet.com",
    
    // License Screen
    licenseTitle = "Open Source Licenses",
    licenseCopyright = "Copyright © 2024 ANAM Labs. All rights reserved.",
    licenseIntro = "This application uses the following open source libraries:",
    licenseApache = "Apache License 2.0",
    licenseMit = "MIT License",
    licenseViewLicense = "View License"
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

/**
 * 스킨과 언어에 따른 Strings 객체 반환
 */
fun getStringsForSkinAndLanguage(skin: Skin, language: Language): Strings {
    return when (language) {
        Language.KOREAN -> getKoreanStringsForSkin(skin)
        Language.ENGLISH -> getEnglishStringsForSkin(skin)
    }
}

/**
 * 스킨별 한국어 문자열
 */
private fun getKoreanStringsForSkin(skin: Skin): Strings {
    val baseStrings = Strings()
    return when (skin) {
        Skin.ANAM -> baseStrings.copy(
            headerTitle = "Anam Wallet",
            headerTitleMain = "Anam Wallet",
            settingsAppInfoDescription = "Anam Wallet에 대해 알아보기",
            loginTitle = "비밀번호를 입력하여 Anam Wallet에 접근하세요"
        )
        Skin.BUSAN -> baseStrings.copy(
            headerTitle = "Busan Wallet",
            headerTitleMain = "Busan Wallet",
            settingsAppInfoDescription = "Busan Wallet에 대해 알아보기",
            loginTitle = "비밀번호를 입력하여 Busan Wallet에 접근하세요"
        )
        // Skin.SEOUL -> baseStrings.copy(
        //     headerTitle = "Seoul Wallet",
        //     headerTitleMain = "Seoul Wallet",
        //     settingsAppInfoDescription = "Seoul Wallet에 대해 알아보기",
        //     loginTitle = "비밀번호를 입력하여 Seoul Wallet에 접근하세요"
        // )
        // Skin.LA -> baseStrings.copy(
        //     headerTitle = "LA Wallet",
        //     headerTitleMain = "LA Wallet",
        //     settingsAppInfoDescription = "LA Wallet에 대해 알아보기",
        //     loginTitle = "비밀번호를 입력하여 LA Wallet에 접근하세요"
        // )
    }
}

/**
 * 스킨별 영어 문자열
 */
private fun getEnglishStringsForSkin(skin: Skin): Strings {
    val baseStrings = EnglishStrings
    return when (skin) {
        Skin.ANAM -> baseStrings.copy(
            headerTitle = "Anam Wallet",
            headerTitleMain = "Anam Wallet",
            settingsAppInfoDescription = "Learn about Anam Wallet",
            loginTitle = "Enter password to access your Anam Wallet"
        )
        Skin.BUSAN -> baseStrings.copy(
            headerTitle = "Busan Wallet",
            headerTitleMain = "Busan Wallet",
            settingsAppInfoDescription = "Learn about Busan Wallet",
            loginTitle = "Enter password to access your Busan Wallet"
        )
        // Skin.SEOUL -> baseStrings.copy(
        //     headerTitle = "Seoul Wallet",
        //     headerTitleMain = "Seoul Wallet",
        //     settingsAppInfoDescription = "Learn about Seoul Wallet",
        //     loginTitle = "Enter password to access your Seoul Wallet"
        // )
        // Skin.LA -> baseStrings.copy(
        //     headerTitle = "LA Wallet",
        //     headerTitleMain = "LA Wallet",
        //     settingsAppInfoDescription = "Learn about LA Wallet",
        //     loginTitle = "Enter password to access your LA Wallet"
        // )
    }
}