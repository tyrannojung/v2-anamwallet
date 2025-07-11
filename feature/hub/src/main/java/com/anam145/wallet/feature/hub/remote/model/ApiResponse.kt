package com.anam145.wallet.feature.hub.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper from AnamHub server
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?
)