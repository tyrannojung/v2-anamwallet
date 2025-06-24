package com.anam145.wallet.core.common.model

/**
 * 미니앱 매니페스트 정보
 * manifest.json 파일의 내용을 담는 데이터 클래스
 */
data class MiniAppManifest(
    val appId: String,
    val type: String,  // "app" or "blockchain"
    val name: String,
    val version: String,
    val icon: String? = null,
    val description: String? = null,
    val mainPage: String? = null,
    val pages: List<String> = emptyList(),  // 미니앱의 모든 페이지 목록 (보안을 위한 화이트리스트)
    val permissions: List<String> = emptyList()
)