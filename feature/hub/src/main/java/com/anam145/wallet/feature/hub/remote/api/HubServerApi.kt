package com.anam145.wallet.feature.hub.remote.api

import com.anam145.wallet.core.common.model.MiniApp
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path


interface HubServerApi {
    @GET("/list")
    suspend fun getMiniAppsList(): List<MiniApp>
    @GET("/download/miniApp/{appId}")
    suspend fun downloadMiniApp(@Path("appId") appId: String): ResponseBody
}