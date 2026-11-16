package com.example.temuin.data.remote

import com.google.gson.annotations.SerializedName

// ---- Envelope ----
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val meta: Meta? = null,
)

data class Meta(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("total_pages") val totalPages: Int,
)

data class ApiErrorEnvelope(val success: Boolean, val error: ApiErrorBody?)
data class ApiErrorBody(val code: String?, val message: String?, val details: Map<String, String>?)

// ---- Auth ----
data class LoginRequest(val identifier: String, val password: String)

data class AuthResult(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_at") val expiresAt: String?,
    val user: UserDto,
)

data class FacultyDto(val code: String, val name: String)

// ---- Users ----
data class UserDto(
    val id: String,
    val name: String,
    val email: String?,
    val nim: String?,
    val faculty: String?,
    val whatsapp: String?,
    val instagram: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
)

/** Partial update: field null tidak dikirim (Gson melewati null). */
data class UpdateProfileRequest(
    val name: String? = null,
    val nim: String? = null,
    val faculty: String? = null,
    val whatsapp: String? = null,
    val instagram: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String,
)

data class UploadResult(val url: String, val size: Long?, val purpose: String?)

// ---- Reports (bagian yang dipakai di Profil) ----
data class PickupPointDto(val id: String, val name: String?, val building: String?)

data class ReportDto(
    val id: String,
    val type: String,            // LOST | FOUND
    val title: String,
    val category: String?,
    @SerializedName("location_name") val locationName: String?,
    @SerializedName("photo_url") val photoUrl: String?,
    val status: String,          // ACTIVE | COMPLETED | CLOSED
    val custody: String?,
    @SerializedName("pickup_point") val pickupPoint: PickupPointDto?,
    @SerializedName("created_at") val createdAt: String?,
)
