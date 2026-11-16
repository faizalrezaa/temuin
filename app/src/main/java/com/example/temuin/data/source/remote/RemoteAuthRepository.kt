package com.example.temuin.data.source.remote

import com.example.temuin.data.local.TokenManager
import com.example.temuin.data.remote.AuthApiService
import com.example.temuin.data.remote.dto.ApiError
import com.example.temuin.data.remote.dto.AuthResultDto
import com.example.temuin.data.remote.dto.LoginRequest
import com.example.temuin.data.remote.dto.RegisterRequest
import com.example.temuin.data.repository.AuthRepository
import com.google.gson.Gson

class RemoteAuthRepository(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    private val gson = Gson()

    override suspend fun login(identifier: String, password: String): Result<AuthResultDto> {
        return try {
            val response = api.login(LoginRequest(identifier, password))
            if (response.isSuccessful) {
                val result = response.body()?.data
                    ?: return Result.failure(Exception("Response kosong dari server"))
                tokenManager.saveToken(result.access_token)
                Result.success(result)
            } else {
                Result.failure(parseApiError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server. Periksa koneksi internet Anda."))
        }
    }

    override suspend fun register(
        name: String,
        nim: String,
        faculty: String,
        whatsapp: String,
        password: String,
        passwordConfirmation: String
    ): Result<AuthResultDto> {
        return try {
            val response = api.register(
                RegisterRequest(
                    name = name,
                    nim = nim,
                    faculty = faculty,
                    whatsapp = whatsapp,
                    password = password,
                    password_confirmation = passwordConfirmation,
                    agree_terms = true
                )
            )
            if (response.isSuccessful) {
                val result = response.body()?.data
                    ?: return Result.failure(Exception("Response kosong dari server"))
                tokenManager.saveToken(result.access_token)
                Result.success(result)
            } else {
                Result.failure(parseApiError(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server. Periksa koneksi internet Anda."))
        }
    }

    private fun parseApiError(body: String?): Exception {
        if (body.isNullOrBlank()) return Exception("Terjadi kesalahan pada server")
        return try {
            val err = gson.fromJson(body, ApiError::class.java)
            val details = err.error?.details
            if (!details.isNullOrEmpty()) {
                val message = details.entries.joinToString("\n") { (_, v) -> v }
                Exception(message)
            } else {
                Exception(err.error?.message ?: "Terjadi kesalahan pada server")
            }
        } catch (e: Exception) {
            Exception("Terjadi kesalahan. Silakan coba lagi.")
        }
    }
}
