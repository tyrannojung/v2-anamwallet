package com.anam145.wallet.feature.identity.data.remote

import com.anam145.wallet.feature.identity.domain.model.DIDDocument
import com.anam145.wallet.feature.identity.domain.model.RegisterDIDRequest
import com.anam145.wallet.feature.identity.domain.model.RegisterDIDResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * DID API 인터페이스
 */
interface DIDApiService {
    
    /**
     * 사용자 DID 등록
     */
    @POST("dids/user")
    suspend fun registerUserDID(
        @Body request: RegisterDIDRequest
    ): Response<RegisterDIDResponse>
    
    /**
     * DID Document 조회
     */
    @GET("dids/{did}")
    suspend fun getDIDDocument(
        @Path("did") did: String
    ): Response<DIDDocument>
}