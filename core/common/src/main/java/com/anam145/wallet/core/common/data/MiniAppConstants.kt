package com.anam145.wallet.core.common.data

/**
 * MiniApp 모듈에서 사용하는 상수들
 */
object MiniAppConstants {
    // MiniApp types
    const val TYPE_BLOCKCHAIN = "blockchain"
    const val TYPE_APP = "app"
    
    // File paths
    const val MINIAPP_ASSETS_PATH = "miniapps"
    const val MINIAPP_INSTALL_DIR = "miniapps"
    const val MANIFEST_FILE_NAME = "manifest.json"
    const val ICON_PATH = "/assets/icons/app_icon.png"
    
    // Cache
    const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5분
    
    // File extensions
    const val ZIP_EXTENSION = ".zip"
}