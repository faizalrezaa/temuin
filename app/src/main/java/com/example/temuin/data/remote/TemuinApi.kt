package com.example.temuin.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TemuinApi {
    @GET("faculties")
    suspend fun faculties(): ApiResponse<List<FacultyDto>>

    @GET("users/me")
    suspend fun getMe(): ApiResponse<UserDto>

    @PATCH("users/me")
    suspend fun updateMe(@Body body: UpdateProfileRequest): ApiResponse<UserDto>

    @PUT("users/me/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ApiResponse<Any?>

    @GET("users/me/reports")
    suspend fun myReports(
        @Query("status") status: String = "ALL",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiResponse<List<ReportDto>>

    @Multipart
    @POST("uploads")
    suspend fun upload(
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: RequestBody,
    ): ApiResponse<UploadResult>

    // ---- Reports ----
    @GET("reports")
    suspend fun reports(
        @Query("q") q: String? = null,
        @Query("type") type: String? = null,
        @Query("status") status: String = "ACTIVE",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiResponse<List<ReportDto>>

    @GET("reports/{id}")
    suspend fun report(@Path("id") id: String): ApiResponse<ReportDto>

    @POST("reports")
    suspend fun createReport(@Body body: CreateReportRequest): ApiResponse<ReportDto>

    @GET("reports/{id}/contact")
    suspend fun contact(@Path("id") id: String): ApiResponse<ContactDto>

    // ---- Claims ----
    @POST("reports/{id}/claims")
    suspend fun submitClaim(@Path("id") reportId: String, @Body body: SubmitClaimRequest): ApiResponse<ClaimDto>

    @GET("claims/incoming")
    suspend fun incomingClaims(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 100,
    ): ApiResponse<List<ClaimDto>>

    @GET("claims/mine")
    suspend fun myClaims(@Query("limit") limit: Int = 100): ApiResponse<List<ClaimDto>>

    @POST("claims/{id}/approve")
    suspend fun approveClaim(@Path("id") id: String): ApiResponse<ClaimDto>

    @POST("claims/{id}/reject")
    suspend fun rejectClaim(@Path("id") id: String, @Body body: RejectClaimRequest): ApiResponse<ClaimDto>

    // ---- Map ----
    @GET("pickup-points")
    suspend fun pickupPoints(): ApiResponse<List<PickupPointDto>>

    @GET("pickup-points/{id}/items")
    suspend fun pickupPointItems(@Path("id") id: String, @Query("limit") limit: Int = 100): ApiResponse<PickupSummaryDto>
}
