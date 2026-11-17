package com.example.temuin.data.repository

import com.example.temuin.data.local.TokenStore
import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.ChangePasswordRequest
import com.example.temuin.data.remote.FacultyDto
import com.example.temuin.data.remote.ReportDto
import com.example.temuin.data.remote.TemuinApi
import com.example.temuin.data.remote.UpdateProfileRequest
import com.example.temuin.data.remote.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileRepository(private val api: TemuinApi = ApiClient.api) {

    companion object {
        private val _currentUser = MutableStateFlow<UserDto?>(null)
        val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

        fun setCurrentUser(user: UserDto?) {
            _currentUser.value = user
        }
    }

    private suspend fun <T> call(block: suspend () -> T): Result<T> =
        runCatching { block() }.recoverCatching { throw Exception(ApiClient.errorMessage(it)) }

    fun logout() {
        _currentUser.value = null
        TokenStore.clear()
    }

    suspend fun getMe(): Result<UserDto> = call {
        val user = api.getMe().data!!
        _currentUser.value = user
        user
    }

    suspend fun faculties(): Result<List<FacultyDto>> = call { api.faculties().data.orEmpty() }

    suspend fun updateProfile(req: UpdateProfileRequest): Result<UserDto> = call {
        val updated = api.updateMe(req).data!!
        _currentUser.value = updated
        updated
    }

    suspend fun changePassword(current: String, new: String): Result<Unit> = call {
        api.changePassword(ChangePasswordRequest(current, new)); Unit
    }

    suspend fun myReports(): Result<List<ReportDto>> = call { api.myReports().data.orEmpty() }

    suspend fun uploadAvatar(bytes: ByteArray, mimeType: String, fileName: String): Result<String> = call {
        val part = MultipartBody.Part.createFormData(
            "file", fileName, bytes.toRequestBody(mimeType.toMediaType())
        )
        val url = api.upload(part, "avatars".toRequestBody("text/plain".toMediaType())).data!!.url
        _currentUser.value = _currentUser.value?.copy(avatarUrl = url)
        url
    }
}
