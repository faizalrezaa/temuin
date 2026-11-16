package com.example.temuin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.remote.dto.AuthResultDto
import com.example.temuin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ─────────────────────────────────────────────────────────────────

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val result: AuthResultDto) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ── Form state: Login ─────────────────────────────────────────────────────

    private val _loginNipNim = MutableStateFlow("")
    val loginNipNim: StateFlow<String> = _loginNipNim.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    // ── Form state: Signup ────────────────────────────────────────────────────

    private val _signupName = MutableStateFlow("")
    val signupName: StateFlow<String> = _signupName.asStateFlow()

    private val _signupNim = MutableStateFlow("")
    val signupNim: StateFlow<String> = _signupNim.asStateFlow()

    private val _signupFakultas = MutableStateFlow("") // menyimpan kode, misal "FILKOM"
    val signupFakultas: StateFlow<String> = _signupFakultas.asStateFlow()

    private val _signupPhone = MutableStateFlow("")
    val signupPhone: StateFlow<String> = _signupPhone.asStateFlow()

    private val _signupPassword = MutableStateFlow("")
    val signupPassword: StateFlow<String> = _signupPassword.asStateFlow()

    private val _signupConfirmPassword = MutableStateFlow("")
    val signupConfirmPassword: StateFlow<String> = _signupConfirmPassword.asStateFlow()

    // ── Async state ───────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Update functions ──────────────────────────────────────────────────────

    fun updateLoginNipNim(value: String) { _loginNipNim.value = value }
    fun updateLoginPassword(value: String) { _loginPassword.value = value }

    fun updateSignupName(value: String) { _signupName.value = value }
    fun updateSignupNim(value: String) { _signupNim.value = value }
    fun updateSignupFakultas(value: String) { _signupFakultas.value = value }
    fun updateSignupPhone(value: String) { _signupPhone.value = value }
    fun updateSignupPassword(value: String) { _signupPassword.value = value }
    fun updateSignupConfirmPassword(value: String) { _signupConfirmPassword.value = value }

    fun resetUiState() { _uiState.value = AuthUiState.Idle }

    // ── API calls ─────────────────────────────────────────────────────────────

    fun login() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(
                identifier = _loginNipNim.value.trim(),
                password = _loginPassword.value
            )
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Login gagal") }
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(
                name = _signupName.value.trim(),
                nim = _signupNim.value.trim(),
                faculty = _signupFakultas.value,
                whatsapp = _signupPhone.value.trim(),
                password = _signupPassword.value,
                passwordConfirmation = _signupConfirmPassword.value
            )
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Registrasi gagal") }
        }
    }

    // ── Local validation ──────────────────────────────────────────────────────

    /** Kembalikan pesan error jika form login tidak valid, null jika valid. */
    fun validateLoginForm(): String? {
        if (_loginNipNim.value.isBlank()) return "NIP/NIM/Email tidak boleh kosong"
        if (_loginPassword.value.length < 8) return "Kata sandi minimal 8 karakter"
        return null
    }

    /** Kembalikan pesan error jika form signup tidak valid, null jika valid. */
    fun validateSignupForm(agreeTerms: Boolean): String? {
        if (_signupName.value.isBlank()) return "Nama lengkap tidak boleh kosong"
        if (!_signupNim.value.matches(Regex("^[0-9]{8,20}$"))) return "NIM harus 8–20 digit angka"
        if (_signupFakultas.value.isBlank()) return "Pilih fakultas terlebih dahulu"
        if (_signupPhone.value.length < 8) return "Nomor WhatsApp minimal 8 digit"
        if (_signupPassword.value.length < 8) return "Kata sandi minimal 8 karakter"
        if (_signupPassword.value != _signupConfirmPassword.value) return "Konfirmasi kata sandi tidak cocok"
        if (!agreeTerms) return "Anda harus menyetujui Ketentuan Layanan"
        return null
    }
}
