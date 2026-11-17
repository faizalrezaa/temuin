package com.example.temuin.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.remote.FacultyDto
import com.example.temuin.data.remote.ReportDto
import com.example.temuin.data.remote.UpdateProfileRequest
import com.example.temuin.data.remote.UserDto
import com.example.temuin.data.repository.ProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ===================== Profil =====================

data class ProfileUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val user: UserDto? = null,
    val reports: List<ReportDto> = emptyList(),
)

class ProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val user = async { repo.getMe() }
            val reports = async { repo.myReports() }

            val userRes = user.await()
            val reportsRes = reports.await()
            val all = reportsRes.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    loading = false,
                    user = userRes.getOrNull() ?: it.user,
                    reports = all,
                    error = userRes.exceptionOrNull()?.message ?: reportsRes.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun logout() = repo.logout()
}

// ===================== Edit Profil =====================

data class EditProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val uploadingAvatar: Boolean = false,
    val name: String = "",
    val nim: String = "",
    val faculty: String = "",
    val whatsapp: String = "",
    val avatarUrl: String? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val faculties: List<FacultyDto> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
) {
    val passwordMismatch get() = confirmPassword.isNotEmpty() && newPassword != confirmPassword
    val wantsPasswordChange get() = newPassword.isNotEmpty()
}

class EditProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()
    private var original: UserDto? = null

    init {
        viewModelScope.launch {
            val faculties = async { repo.faculties() }
            repo.getMe()
                .onSuccess { u ->
                    original = u
                    _state.update {
                        it.copy(
                            name = u.name, nim = u.nim.orEmpty(), faculty = u.faculty.orEmpty(),
                            whatsapp = u.whatsapp.orEmpty(), avatarUrl = u.avatarUrl,
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
            _state.update { it.copy(loading = false, faculties = faculties.await().getOrDefault(emptyList())) }
        }
    }

    fun onName(v: String) = _state.update { it.copy(name = v) }
    fun onNim(v: String) = _state.update { it.copy(nim = v.filter(Char::isDigit)) }
    fun onFaculty(v: String) = _state.update { it.copy(faculty = v) }
    fun onWhatsapp(v: String) = _state.update { it.copy(whatsapp = v) }
    fun onCurrentPassword(v: String) = _state.update { it.copy(currentPassword = v) }
    fun onNewPassword(v: String) = _state.update { it.copy(newPassword = v) }
    fun onConfirmPassword(v: String) = _state.update { it.copy(confirmPassword = v) }
    fun dismissError() = _state.update { it.copy(error = null) }

    fun uploadAvatar(bytes: ByteArray, mime: String) {
        if (bytes.size > 5 * 1024 * 1024) {
            _state.update { it.copy(error = "Ukuran foto maksimal 5MB") }
            return
        }
        _state.update { it.copy(uploadingAvatar = true) }
        viewModelScope.launch {
            val ext = mime.substringAfter('/', "jpg")
            repo.uploadAvatar(bytes, mime, "avatar.$ext")
                .onSuccess { url -> _state.update { it.copy(uploadingAvatar = false, avatarUrl = url) } }
                .onFailure { e -> _state.update { it.copy(uploadingAvatar = false, error = e.message) } }
        }
    }

    fun save() {
        val s = _state.value
        validate(s)?.let { msg -> _state.update { it.copy(error = msg) }; return }

        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val o = original
            // Kirim hanya field yang berubah (PATCH partial)
            val req = UpdateProfileRequest(
                name = s.name.trim().takeIf { it != o?.name },
                nim = s.nim.takeIf { it != o?.nim.orEmpty() },
                faculty = s.faculty.takeIf { it != o?.faculty.orEmpty() },
                whatsapp = s.whatsapp.trim().takeIf { it != o?.whatsapp.orEmpty() },
                avatarUrl = s.avatarUrl.takeIf { it != o?.avatarUrl },
            )
            val hasProfileChange = listOf(req.name, req.nim, req.faculty, req.whatsapp, req.avatarUrl).any { it != null }

            if (hasProfileChange) {
                val res = repo.updateProfile(req)
                res.onSuccess { original = it }
                if (res.isFailure) {
                    _state.update { it.copy(saving = false, error = res.exceptionOrNull()?.message) }
                    return@launch
                }
            }
            if (s.wantsPasswordChange) {
                val res = repo.changePassword(s.currentPassword, s.newPassword)
                if (res.isFailure) {
                    _state.update { it.copy(saving = false, error = res.exceptionOrNull()?.message) }
                    return@launch
                }
            }
            _state.update { it.copy(saving = false, saved = true) }
        }
    }

    private fun validate(s: EditProfileUiState): String? = when {
        s.name.trim().length < 2 -> "Nama minimal 2 karakter"
        s.nim.isNotEmpty() && s.nim.length !in 8..20 -> "NIM harus 8–20 digit angka"
        s.whatsapp.isNotEmpty() && s.whatsapp.trim().length < 8 -> "Nomor WhatsApp tidak valid"
        s.wantsPasswordChange && s.currentPassword.isEmpty() -> "Masukkan kata sandi saat ini"
        s.wantsPasswordChange && s.newPassword.length < 8 -> "Kata sandi baru minimal 8 karakter"
        s.passwordMismatch -> "Konfirmasi kata sandi tidak sama"
        else -> null
    }
}
