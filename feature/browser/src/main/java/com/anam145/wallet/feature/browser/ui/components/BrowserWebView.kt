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
            // JavaScript Bridge 추가
            addJavascriptInterface(jsBridge, "_native")
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
                    
                    // 페이지 로드 완료 후 브리지 주입
                    view?.let { webView ->
                        // 1. Universal Bridge 주입
                        injectUniversalBridge(webView)
                        
                        // 2. 블록체인별 Bridge 스크립트 주입
                        bridgeScript?.let { script ->
                            webView.evaluateJavascript(script) { result ->
                                Log.d("BrowserWebView", "Bridge script injected (len=${script.length}) -> $result")
                            }
                        }
                        
                        // 3. 디버깅: 주입 확인
                        webView.evaluateJavascript("""
                            (function(){
                                console.log('[DEBUG] has _anamBridge:', !!window._anamBridge);
                                console.log('[DEBUG] has ethereum:', !!window.ethereum);
                                console.log('[DEBUG] isAnamWallet:', window.ethereum && window.ethereum.isAnamWallet);
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

/**
 * Universal Bridge 스크립트 가져오기
 */
internal fun getUniversalBridgeScript(): String {
    return """
        (function() {
            if (window._anamBridge) return;
            
            console.log('[AnamWallet] Installing Universal Bridge');
            
            window._anamBridge = {
                _callbacks: {},
                _timeout: 30000,
                
                universalBridge: function(requestId, payload) {
                    console.log('[Bridge] Request:', requestId, payload);
                    
                    return new Promise((resolve, reject) => {
                        // 콜백 저장
                        this._callbacks[requestId] = { resolve, reject };
                        
                        // Native 호출
                        try {
                            window._native.universalBridge(requestId, payload);
                        } catch (e) {
                            console.error('[Bridge] Native call failed:', e);
                            reject(e);
                            delete this._callbacks[requestId];
                            return;
                        }
                        
                        // 타임아웃 설정
                        setTimeout(() => {
                            if (this._callbacks[requestId]) {
                                console.warn('[Bridge] Request timeout:', requestId);
                                reject(new Error('Request timeout'));
                                delete this._callbacks[requestId];
                            }
                        }, this._timeout);
                    });
                },
                
                handleResponse: function(requestId, response) {
                    console.log('[Bridge] Response:', requestId, response);
                    
                    const callback = this._callbacks[requestId];
                    if (!callback) {
                        console.warn('[Bridge] No callback for:', requestId);
                        return;
                    }
                    
                    try {
                        // Android now always passes parsed objects
                        // response is guaranteed to be { jsonrpc, id, result | error }
                        if (response.error) {
                            callback.reject(response.error);
                        } else {
                            callback.resolve(response);
                        }
                    } catch (e) {
                        console.error('[Bridge] Response handling error:', e);
                        callback.reject(e);
                    }
                    
                    delete this._callbacks[requestId];
                }
            };
            
            console.log('[AnamWallet] Universal Bridge ready');
        })();
    """.trimIndent()
}

/**
 * 기본 Universal Bridge 주입
 */
private fun injectUniversalBridge(webView: WebView?) {
    webView?.evaluateJavascript(getUniversalBridgeScript(), null)
}