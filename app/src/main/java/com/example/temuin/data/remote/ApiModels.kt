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
data class PickupPointDto(
    val id: String,
    val code: String? = null,
    val name: String?,
    val description: String? = null,
    val building: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("opening_hours") val openingHours: String? = null,
)

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
    val color: String? = null,
    val description: String? = null,
    @SerializedName("occurred_at") val occurredAt: String? = null,
    @SerializedName("location_area") val locationArea: String? = null,
    val owner: UserPublicDto? = null,
    val viewer: ReportViewerDto? = null,
)

data class UserPublicDto(
    val id: String,
    val name: String?,
    val faculty: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
)

data class ReportViewerDto(
    @SerializedName("is_owner") val isOwner: Boolean = false,
    @SerializedName("can_claim") val canClaim: Boolean = false,
    @SerializedName("can_contact") val canContact: Boolean = false,
    @SerializedName("my_claim_id") val myClaimId: String? = null,
    @SerializedName("my_claim_status") val myClaimStatus: String? = null,
)

data class CreateReportRequest(
    val type: String,
    val title: String,
    val category: String,
    val description: String,
    @SerializedName("occurred_at") val occurredAt: String,
    @SerializedName("location_name") val locationName: String? = null,
    @SerializedName("location_point_id") val locationPointId: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    val color: String? = null,
    val custody: String? = null,
    @SerializedName("pickup_point_id") val pickupPointId: String? = null,
)

data class ContactDto(
    val name: String?,
    val whatsapp: String?,
    val instagram: String?,
    @SerializedName("whatsapp_link") val whatsappLink: String?,
)

// ---- Claims ----
data class SubmitClaimRequest(
    @SerializedName("secret_description") val secretDescription: String,
    @SerializedName("proof_photo_url") val proofPhotoUrl: String? = null,
)

data class RejectClaimRequest(val reason: String)

data class ClaimReportSummaryDto(
    val id: String,
    val title: String?,
    @SerializedName("photo_url") val photoUrl: String?,
)

data class PickupInfoDto(
    val method: String?,
    @SerializedName("pickup_point") val pickupPoint: PickupPointDto?,
    val instructions: String?,
)

data class ClaimDto(
    val id: String,
    val status: String,
    val report: ClaimReportSummaryDto?,
    val claimant: UserPublicDto?,
    val finder: UserPublicDto?,
    @SerializedName("secret_description") val secretDescription: String?,
    @SerializedName("rejection_reason") val rejectionReason: String?,
    val pickup: PickupInfoDto?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
)

data class PickupSummaryDto(
    @SerializedName("pickup_point") val pickupPoint: PickupPointDto?,
    @SerializedName("available_count") val availableCount: Int,
    val items: List<ReportDto>?,
)

data class CategoryDto(val code: String, val label: String)

// ---- Match recommendations ----
data class MatchResultDto(
    val source: String? = null,
    val notices: List<String>? = null,
    @SerializedName("candidates_found") val candidatesFound: Int? = null,
    val items: List<MatchItemDto>? = null,
)

data class MatchItemDto(
    val report: MatchReportDto? = null,
    val level: String? = null,
    @SerializedName("matched_attributes") val matchedAttributes: List<String>? = null,
    @SerializedName("different_attributes") val differentAttributes: List<String>? = null,
    @SerializedName("not_comparable_attributes") val notComparableAttributes: List<String>? = null,
    val reason: String? = null,
    @SerializedName("pre_score") val preScore: Double? = null,
    @SerializedName("incomplete_data") val incompleteData: Boolean? = null,
)

data class MatchReportDto(
    val id: String? = null,
    val title: String? = null,
    val category: String? = null,
    val color: String? = null,
    val description: String? = null,
    @SerializedName("occurred_at") val occurredAt: String? = null,
    @SerializedName("location_name") val locationName: String? = null,
    @SerializedName("owner_name") val ownerName: String? = null,
)
