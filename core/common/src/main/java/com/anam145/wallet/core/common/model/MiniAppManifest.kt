package com.anam145.wallet.core.common.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MiniAppManifest(
    @SerialName("app_id") val appId: String,
    val name: String,
    val version: String,
    val type: String, // "blockchain" or "app"
    @SerialName("main_page") val mainPage: String? = null,
    val pages: List<String> = emptyList(),
    val permissions: List<String> = emptyList()
)