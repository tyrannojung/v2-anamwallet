package com.anam145.wallet.feature.identity.data.remote

import com.anam145.wallet.feature.identity.domain.model.*
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
    
    /**
     * 학생증 발급
     */
    @POST("students")
    suspend fun issueStudentCard(
        @Body request: IssueStudentCardRequest
    ): Response<StudentCardResponse>
    
    /**
     * 운전면허증 발급
     */
    @POST("licenses")
    suspend fun issueDriverLicense(
        @Body request: IssueDriverLicenseRequest
    ): Response<DriverLicenseResponse>
    
    /**
     * VC 조회
     */
    @GET("vcs/{vcId}")
    suspend fun getVerifiableCredential(
        @Path("vcId") vcId: String
    ): Response<VerifiableCredential>
    
    /**
     * VC 조회 (간략한 메서드명)
     */
    @GET("vcs/{vcId}")
    suspend fun getVC(
        @Path("vcId") vcId: String
    ): Response<VerifiableCredential>
}