package com.example.temuin.data.remote

import com.example.temuin.data.remote.dto.ApiEnvelope
import com.example.temuin.data.remote.dto.AuthResultDto
import com.example.temuin.data.remote.dto.LoginRequest
import com.example.temuin.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiEnvelope<AuthResultDto>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiEnvelope<AuthResultDto>>
}
