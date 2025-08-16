package com.anam145.wallet.feature.browser.domain.model

import kotlinx.serialization.Serializable

/**
 * 북마크 도메인 모델
 */
@Serializable
data class Bookmark(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)