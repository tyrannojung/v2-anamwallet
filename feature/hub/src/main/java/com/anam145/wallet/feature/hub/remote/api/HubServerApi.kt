package com.anam145.wallet.feature.hub.remote.api

<<<<<<< HEAD
import com.anam145.wallet.core.common.model.MiniApp
=======
import com.anam145.wallet.feature.hub.remote.model.ApiResponse
import com.anam145.wallet.feature.hub.remote.model.HubMiniAppResponse
>>>>>>> feature/blockchain-modular
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface HubServerApi {
<<<<<<< HEAD
    @GET("/list")
    suspend fun getMiniAppsList(): List<MiniApp>
    @GET("/download/miniApp/{appId}")
=======
    @GET("/miniapps")
    suspend fun getMiniApps(): ApiResponse<List<HubMiniAppResponse>>
    
    @GET("/miniapps/{appId}")
    suspend fun getMiniApp(@Path("appId") appId: String): ApiResponse<HubMiniAppResponse>
    
    @GET("/miniapps/{appId}/download")
>>>>>>> feature/blockchain-modular
    suspend fun downloadMiniApp(@Path("appId") appId: String): Response<ResponseBody>
}