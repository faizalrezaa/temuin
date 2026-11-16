package com.example.temuin.data.repository

import com.example.temuin.data.remote.dto.AuthResultDto

interface AuthRepository {
    suspend fun login(identifier: String, password: String): Result<AuthResultDto>
    suspend fun register(
        name: String,
        nim: String,
        faculty: String,
        whatsapp: String,
        password: String,
        passwordConfirmation: String
    ): Result<AuthResultDto>
}
