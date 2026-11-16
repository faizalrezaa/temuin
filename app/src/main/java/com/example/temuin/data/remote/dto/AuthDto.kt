package com.example.temuin.data.remote.dto

// ── Request ──────────────────────────────────────────────────────────────────

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val nim: String,
    val faculty: String,
    val whatsapp: String,
    val password: String,
    val password_confirmation: String,
    val agree_terms: Boolean = true
)

// ── Response ─────────────────────────────────────────────────────────────────

data class UserPrivateDto(
    val id: String,
    val name: String,
    val email: String?,
    val nim: String?,
    val faculty: String?,
    val whatsapp: String?,
    val instagram: String?,
    val avatar_url: String?,
    val created_at: String?,
    val updated_at: String?
)

data class AuthResultDto(
    val access_token: String,
    val token_type: String,
    val expires_at: String?,
    val user: UserPrivateDto
)



data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class ApiError(
    val success: Boolean,
    val error: ErrorBody?
) {
    data class ErrorBody(
        val code: String?,
        val message: String?,
        val details: Map<String, String>?
    )
}
