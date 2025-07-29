package com.anam145.wallet.feature.miniapp.common.webview

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import java.io.File
import androidx.core.net.toUri

/**
 * WebView 생성을 위한 Factory 클래스
 * 
 * BlockchainService, BlockchainUIActivity, WebAppActivity에서
 * 중복되는 WebView 생성 로직을 통합합니다.
 */
object WebViewFactory {
    
    /**
     * 설정이 완료된 WebView 인스턴스를 생성합니다.
     * 
     * @param context Context (Application context 권장)
     * @param appId 미니앱 또는 블록체인 ID
     * @param baseDir 미니앱의 base directory
     * @param headless 헤드리스 모드 여부
     * @param jsBridge JavaScript 인터페이스 객체
     * @param enableDebugging 디버깅 활성화 여부
     * @return 설정이 완료된 WebView
     */
    fun create(
        context: Context,
        appId: String,
        baseDir: File,
        headless: Boolean = false,
        jsBridge: Any? = null,
        enableDebugging: Boolean = false
    ): WebView {
        // AssetLoader 생성
        val assetLoader = MiniAppAssetLoader.create(appId, baseDir)
        
        return WebView(context).apply {
            // 공통 설정 적용
            if (headless) {
                WebViewConfigurer.configureHeadless(this, enableDebugging)
            } else {
                WebViewConfigurer.configure(this, enableDebugging)
            }
            
            // WebViewClient 설정
            webViewClient = createWebViewClient(assetLoader)
            
            
            // JavaScript Bridge 추가
            jsBridge?.let {
                @Suppress("JavascriptInterface")
                addJavascriptInterface(it, "anam")
            }
            
            // 추가 UI 설정 (headless가 아닌 경우)
            if (!headless) {
                visibility = android.view.View.VISIBLE
                setBackgroundColor(android.graphics.Color.WHITE)
            }
        }
    }
    
    /**
     * AssetLoader를 사용하는 WebViewClient 생성
     */
    private fun createWebViewClient(assetLoader: WebViewAssetLoader): WebViewClient {
        return object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
            
            @Suppress("DEPRECATION")
            override fun shouldInterceptRequest(
                view: WebView,
                url: String
            ): android.webkit.WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(url.toUri())
            }
        }
    }
}