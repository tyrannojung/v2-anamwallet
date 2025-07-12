package com.anam145.wallet.feature.hub.remote.api

import com.anam145.wallet.feature.hub.remote.model.ApiResponse
import com.anam145.wallet.feature.hub.remote.model.HubMiniAppResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface HubServerApi {
    @GET("/miniapps")
    suspend fun getMiniApps(): ApiResponse<List<HubMiniAppResponse>>
    
    @GET("/miniapps/{appId}")
    suspend fun getMiniApp(@Path("appId") appId: String): ApiResponse<HubMiniAppResponse>
    
    @GET("/miniapps/{appId}/download")
    suspend fun downloadMiniApp(@Path("appId") appId: String): Response<ResponseBody>
}