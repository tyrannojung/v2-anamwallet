package com.anam145.wallet.core.common.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("mini_app")
data class MiniApp(
    @PrimaryKey val appId: String,
    val name: String,
    val type: MiniAppType,
    val iconPath: String? = null, // Path to icon instead of Bitmap
    val balance: String? = null // For blockchain type
)