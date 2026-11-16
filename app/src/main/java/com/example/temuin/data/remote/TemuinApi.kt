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
}
