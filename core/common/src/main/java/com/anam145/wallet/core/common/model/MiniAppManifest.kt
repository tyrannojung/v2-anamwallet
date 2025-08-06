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
    val permissions: List<String> = emptyList(),
    val bridge: BridgeConfig? = null  // 브라우저 브릿지 설정 (선택적)
)

/**
 * 브라우저 브릿지 설정
 * DApp 통합을 위한 JavaScript 브릿지 스크립트 정보
 */
data class BridgeConfig(
    val script: String  // 브릿지 스크립트 파일 경로 (예: "bridge/dapp-bridge.js")
)