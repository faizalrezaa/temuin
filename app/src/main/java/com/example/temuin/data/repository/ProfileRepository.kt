package com.example.temuin.data.repository

import com.example.temuin.data.local.TokenStore
import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.ChangePasswordRequest
import com.example.temuin.data.remote.FacultyDto
import com.example.temuin.data.remote.ReportDto
import com.example.temuin.data.remote.TemuinApi
import com.example.temuin.data.remote.UpdateProfileRequest
import com.example.temuin.data.remote.UserDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileRepository(private val api: TemuinApi = ApiClient.api) {

    private suspend fun <T> call(block: suspend () -> T): Result<T> =
        runCatching { block() }.recoverCatching { throw Exception(ApiClient.errorMessage(it)) }

    fun logout() = TokenStore.clear()

    suspend fun getMe(): Result<UserDto> = call { api.getMe().data!! }

    suspend fun faculties(): Result<List<FacultyDto>> = call { api.faculties().data.orEmpty() }

    suspend fun updateProfile(req: UpdateProfileRequest): Result<UserDto> = call { api.updateMe(req).data!! }

    suspend fun changePassword(current: String, new: String): Result<Unit> = call {
        api.changePassword(ChangePasswordRequest(current, new)); Unit
    }

    suspend fun myReports(): Result<List<ReportDto>> = call { api.myReports().data.orEmpty() }

    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String, fileName: String): Result<String> = call {
        val part = MultipartBody.Part.createFormData(
            "file", fileName, bytes.toRequestBody(mimeType.toMediaType())
        )
        api.upload(part, "avatars".toRequestBody("text/plain".toMediaType())).data!!.url
    }
}
