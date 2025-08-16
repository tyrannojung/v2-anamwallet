package com.anam145.wallet.core.common.model

data class MiniApp(
    val appId: String,
    val name: String,
    val type: MiniAppType,
    val iconPath: String? = null, // Path to icon instead of Bitmap
    val bridge: BridgeConfig? = null // Bridge configuration from manifest
)