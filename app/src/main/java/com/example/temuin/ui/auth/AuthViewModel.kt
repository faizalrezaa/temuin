package com.example.temuin.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SignupErrors(
    val name: String? = null,
    val nim: String? = null,
    val fakultas: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val terms: String? = null
) {
    val hasErrors: Boolean
        get() = listOf(name, nim, fakultas, phone, password, confirmPassword, terms)
            .any { it != null }
}

data class SignupUiState(
    val name: String = "",
    val nim: String = "",
    val fakultas: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val submitAttempted: Boolean = false,
    val errors: SignupErrors = SignupErrors()
)

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val submitAttempted: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null
)

data class FormSubmission(
    val title: String,
    val combinedIdentity: String,
    val contact: String,
    val source: String
)

class AuthViewModel : ViewModel() {
    private val _signupUiState = MutableStateFlow(SignupUiState())
    val signupUiState = _signupUiState.asStateFlow()

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState = _loginUiState.asStateFlow()

    private val _submission = MutableStateFlow<FormSubmission?>(null)
    val submission = _submission.asStateFlow()

    val fakultasOptions = listOf(
        "Fakultas Ilmu Komputer",
        "Fakultas Teknik",
        "Fakultas Ekonomi dan Bisnis",
        "Fakultas Hukum"
    )

    fun updateSignupName(value: String) = updateSignup { it.copy(name = value) }
    fun updateSignupNim(value: String) = updateSignup { it.copy(nim = value) }
    fun updateSignupFakultas(value: String) = updateSignup { it.copy(fakultas = value) }
    fun updateSignupPhone(value: String) = updateSignup { it.copy(phone = value) }
    fun updateSignupPassword(value: String) = updateSignup { it.copy(password = value) }
    fun updateSignupConfirmPassword(value: String) = updateSignup { it.copy(confirmPassword = value) }
    fun updateTermsAccepted(value: Boolean) = updateSignup { it.copy(termsAccepted = value) }

    fun updateLoginNipNim(value: String) = updateLogin { it.copy(identifier = value) }
    fun updateLoginPassword(value: String) = updateLogin { it.copy(password = value) }

    fun submitSignup(): Boolean {
        val current = _signupUiState.value
        val errors = validateSignup(current)
        _signupUiState.value = current.copy(submitAttempted = true, errors = errors)
        if (errors.hasErrors) return false

        val cleanName = current.name.trim().replace(Regex("\\s+"), " ")
        val cleanNim = current.nim.trim()
        val contact = normalizePhone(current.phone)
        _submission.value = FormSubmission(
            title = "Data pendaftaran berhasil diolah",
            combinedIdentity = "$cleanName • $cleanNim • ${current.fakultas}",
            contact = contact,
            source = "Nama + NIM + fakultas digabungkan menjadi identitas akun. " +
                "Nomor telepon dinormalisasi ke format +62."
        )
        return true
    }

    fun submitLogin(): Boolean {
        val checked = validateLogin(_loginUiState.value.copy(submitAttempted = true))
        _loginUiState.value = checked
        if (checked.identifierError != null || checked.passwordError != null) return false

        _submission.value = FormSubmission(
            title = "Form masuk lolos validasi lokal",
            combinedIdentity = checked.identifier.trim(),
            contact = "Tidak ada autentikasi server pada demo ini.",
            source = "Identitas dan kata sandi diperiksa formatnya di ViewModel. " +
                "Pemeriksaan ini belum membuktikan kredensial benar."
        )
        return true
    }

    private fun updateSignup(transform: (SignupUiState) -> SignupUiState) {
        _signupUiState.update { current ->
            val next = transform(current)
            next.copy(errors = if (next.submitAttempted) validateSignup(next) else SignupErrors())
        }
    }

    private fun updateLogin(transform: (LoginUiState) -> LoginUiState) {
        _loginUiState.update { current ->
            val next = transform(current)
            if (next.submitAttempted) validateLogin(next) else next
        }
    }

    private fun validateSignup(state: SignupUiState): SignupErrors = SignupErrors(
        name = when {
            state.name.isBlank() -> "Nama lengkap wajib diisi."
            state.name.count { it.isLetter() } < 2 -> "Nama harus memuat minimal dua huruf."
            state.name.any { !it.isLetter() && !it.isWhitespace() && it !in "'.-" } ->
                "Nama hanya boleh berisi huruf, spasi, titik, apostrof, atau tanda hubung."
            else -> null
        },
        nim = when {
            state.nim.isBlank() -> "NIM wajib diisi."
            !state.nim.trim().matches(Regex("[0-9]{15}")) -> "NIM harus terdiri dari 15 digit."
            else -> null
        },
        fakultas = when {
            state.fakultas.isBlank() -> "Pilih fakultas terlebih dahulu."
            state.fakultas !in fakultasOptions -> "Pilih fakultas dari daftar yang tersedia."
            else -> null
        },
        phone = when {
            state.phone.isBlank() -> "Nomor telepon wajib diisi."
            !normalizePhone(state.phone).matches(Regex("\\+628[0-9]{8,11}")) ->
                "Gunakan nomor seluler 08, 628, atau +628 dengan 10–13 digit format lokal."
            else -> null
        },
        password = passwordError(state.password),
        confirmPassword = when {
            state.confirmPassword.isEmpty() -> "Konfirmasi kata sandi wajib diisi."
            state.confirmPassword != state.password -> "Konfirmasi kata sandi tidak sama."
            else -> null
        },
        terms = if (state.termsAccepted) null else "Centang persetujuan sebelum melanjutkan."
    )

    private fun validateLogin(state: LoginUiState): LoginUiState {
        val identifier = state.identifier.trim()
        val isCampusEmail = identifier.matches(
            Regex("[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)*ub\\.ac\\.id", RegexOption.IGNORE_CASE)
        )
        val identifierError = when {
            identifier.isEmpty() -> "NIM, NIP, atau email kampus wajib diisi."
            !identifier.matches(Regex("(?:[0-9]{15}|[0-9]{18})")) && !isCampusEmail ->
                "Gunakan NIM 15 digit, NIP 18 digit, atau email berdomain ub.ac.id."
            else -> null
        }
        return state.copy(
            identifierError = identifierError,
            passwordError = passwordError(state.password)
        )
    }

    private fun passwordError(value: String): String? = when {
        value.isBlank() -> "Kata sandi wajib diisi."
        value.length < 8 -> "Kata sandi minimal 8 karakter."
        else -> null
    }

    private fun normalizePhone(value: String): String {
        val compact = value.trim().replace(Regex("[\\s().-]"), "")
        return when {
            compact.startsWith("0") -> "+62${compact.drop(1)}"
            compact.startsWith("62") -> "+$compact"
            else -> compact
        }
    }
}
