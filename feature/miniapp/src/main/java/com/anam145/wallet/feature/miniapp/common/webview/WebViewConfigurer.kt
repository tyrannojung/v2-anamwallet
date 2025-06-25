package com.anam145.wallet.feature.miniapp.common.webview

import android.webkit.WebSettings
import android.webkit.WebView

/**
 * WebView 공통 설정을 담당하는 유틸리티 클래스
 * 
 * BlockchainService, BlockchainUIActivity, WebAppActivity에서
 * 중복되는 WebView 설정을 통합 관리합니다.
 */
object WebViewConfigurer {
    
    /**
     * WebView의 기본 설정을 적용합니다.
     * 
     * @param webView 설정을 적용할 WebView
     * @param enableDebugging 디버깅 활성화 여부 (기본값: false - 보안상 명시적으로 활성화해야 함)
     */
    fun configure(
        webView: WebView,
        enableDebugging: Boolean = false
    ) {
        // WebView 디버깅 설정 - 명시적으로 요청된 경우에만 활성화
        // 경고: 프로덕션 빌드에서는 절대 true로 설정하지 마세요!
        if (enableDebugging) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        
        webView.settings.apply {
            // JavaScript 활성화 (필수)
            javaScriptEnabled = true
            
            // DOM Storage 활성화 (localStorage 등)
            domStorageEnabled = true
            
            // Viewport 설정
            loadWithOverviewMode = true
            useWideViewPort = true
            
            // 줌 컨트롤 비활성화
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            
            // 보안 설정: 파일 시스템 접근 차단
            allowFileAccess = false
            allowContentAccess = false
            
            // Mixed content 차단 (HTTPS에서 HTTP 리소스 로드 차단)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            
            // 캐시 설정
            cacheMode = WebSettings.LOAD_NO_CACHE
            
            // 텍스트 자동 크기 조절 비활성화
            textZoom = 100
        }
    }
    
    /**
     * 헤드리스 WebView를 위한 추가 설정
     * 
     * @param webView 설정을 적용할 WebView
     * @param enableDebugging 디버깅 활성화 여부 (기본값: false - 보안상 명시적으로 활성화해야 함)
     */
    fun configureHeadless(webView: WebView, enableDebugging: Boolean = false) {
        configure(webView, enableDebugging)
        
        webView.settings.apply {
            // 헤드리스 모드에서는 미디어 자동 재생 허용
            mediaPlaybackRequiresUserGesture = false
            
            // 이미지 로드 비활성화 (성능 최적화)
            loadsImagesAutomatically = false
            blockNetworkImage = true
        }
    }
}