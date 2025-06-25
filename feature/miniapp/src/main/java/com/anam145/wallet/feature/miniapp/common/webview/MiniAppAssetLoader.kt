package com.anam145.wallet.feature.miniapp.common.webview

import androidx.webkit.WebViewAssetLoader
import java.io.File

/**
 * MiniApp용 WebViewAssetLoader 생성을 담당하는 유틸리티 클래스
 * 
 * 각 미니앱별 고유 도메인과 경로 핸들러 설정을 통합 관리합니다.
 */
object MiniAppAssetLoader {
    
    /**
     * 미니앱용 WebViewAssetLoader를 생성합니다.
     * 
     * @param appId 미니앱 또는 블록체인 ID
     * @param baseDir 미니앱의 base directory
     * @return 설정이 완료된 WebViewAssetLoader
     */
    fun create(appId: String, baseDir: File): WebViewAssetLoader {
        return WebViewAssetLoader.Builder()
            .setDomain("$appId.miniapp.local")
            .addPathHandler("/", InternalStoragePathHandler(baseDir))
            .build()
    }
}