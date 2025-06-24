package com.anam145.wallet.core.common.extension

import com.anam145.wallet.core.common.model.MiniAppManifest

/**
 * MiniAppManifest의 진입점(메인 페이지)을 결정합니다.
 * 
 * 우선순위:
 * 1. manifest의 mainPage
 * 2. pages 목록의 첫 번째 페이지
 * 3. 기본값 "index.html"
 * 
 * @return 진입점 페이지 경로 (.html 확장자 포함)
 */
fun MiniAppManifest.resolveEntryPoint(): String {
    return when {
        !mainPage.isNullOrBlank() -> mainPage
        pages.isNotEmpty() -> {
            val firstPage = pages.first()
            if (firstPage.endsWith(".html")) firstPage else "$firstPage.html"
        }
        else -> "index.html"
    }
}