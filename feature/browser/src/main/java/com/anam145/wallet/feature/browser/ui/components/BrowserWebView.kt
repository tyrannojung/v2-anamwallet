package com.anam145.wallet.feature.browser.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.anam145.wallet.feature.browser.bridge.BrowserJavaScriptBridge

/**
 * WebView 컴포저블
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    url: String,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {},
    onPageStarted: (String) -> Unit = {},
    onPageFinished: (String) -> Unit = {},
    onPageError: (String) -> Unit = {},
    onUniversalRequest: ((String, String) -> Unit)? = null,
    bridgeScript: String? = null
) {
    val context = LocalContext.current
    val strings = com.anam145.wallet.core.ui.language.LocalStrings.current
    
    // JavaScript Bridge 생성
    val jsBridge = remember {
        BrowserJavaScriptBridge(
            context = context,
            onUniversalRequest = onUniversalRequest
        )
    }
    
    val webView = remember {
        WebView(context).apply {
            // JavaScript Bridge 추가 (v2.0)
            // @JavascriptInterface 경고 억제: BrowserJavaScriptBridge 클래스에서
            // universalBridge 메서드만 의도적으로 @JavascriptInterface를 가지고 있음.
            // sendUniversalResponse와 webView 프로퍼티는 JavaScript에 노출되지 않으며,
            // Native에서만 사용되는 것이 설계 의도임.
            @Suppress("JavascriptInterface")
            addJavascriptInterface(jsBridge, "WalletNative")  // v2.0: WalletNative만 제공
            tag = jsBridge
            jsBridge.webView = this
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                cacheMode = WebSettings.LOAD_DEFAULT
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    url?.let { onPageStarted(it) }
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    url?.let { onPageFinished(it) }
                    
                    // 페이지 로드 완료 후 블록체인 브리지만 주입 (v2.0)
                    view?.let { webView ->
                        // Universal Bridge 주입 제거 - v2.0에서는 JS가 직접 구현
                        // 블록체인별 Bridge 스크립트 주입 (WalletBridge 포함)
                        bridgeScript?.let { script ->
                            webView.evaluateJavascript(script) { result ->
                                Log.d("BrowserWebView", "Bridge script injected (v2.0) -> $result")
                            }
                        }
                        
                        // 디버깅: v2.0 주입 확인
                        webView.evaluateJavascript("""
                            (function(){
                                console.log('[DEBUG v2.0] has WalletNative:', !!window.WalletNative);
                                console.log('[DEBUG v2.0] has WalletBridge:', !!window.WalletBridge);
                                console.log('[DEBUG v2.0] has ethereum:', !!window.ethereum);
                                console.log('[DEBUG v2.0] isAnamWallet:', window.ethereum && window.ethereum.isAnamWallet);
                            })();
                        """.trimIndent(), null)
                    }
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        onPageError(strings.browserPageLoadError)
                    }
                }
                
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                }
            }
        }
    }
    
    AndroidView(
        factory = {
            webView.also { onWebViewCreated(it) }
        },
        modifier = modifier,
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}

// v2.0: Universal Bridge 스크립트 제거됨
// 이제 블록체인 개발자가 dapp-bridge.js에서 WalletBridge를 직접 구현합니다.
// Native는 WalletNative 인터페이스만 제공하고, 나머지는 JavaScript가 처리합니다.