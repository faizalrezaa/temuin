package com.example.temuin.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    // State untuk Login
    private val _loginNipNim = MutableStateFlow("")
    val loginNipNim: StateFlow<String> = _loginNipNim.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    // State untuk Signup
    private val _signupName = MutableStateFlow("")
    val signupName: StateFlow<String> = _signupName.asStateFlow()

    private val _signupNim = MutableStateFlow("")
    val signupNim: StateFlow<String> = _signupNim.asStateFlow()

    private val _signupFakultas = MutableStateFlow("")
    val signupFakultas: StateFlow<String> = _signupFakultas.asStateFlow()

    private val _signupPhone = MutableStateFlow("")
    val signupPhone: StateFlow<String> = _signupPhone.asStateFlow()

    private val _signupPassword = MutableStateFlow("")
    val signupPassword: StateFlow<String> = _signupPassword.asStateFlow()

    private val _signupConfirmPassword = MutableStateFlow("")
    val signupConfirmPassword: StateFlow<String> = _signupConfirmPassword.asStateFlow()

    // Update functions
    fun updateLoginNipNim(value: String) { _loginNipNim.value = value }
    fun updateLoginPassword(value: String) { _loginPassword.value = value }

    fun updateSignupName(value: String) { _signupName.value = value }
    fun updateSignupNim(value: String) { _signupNim.value = value }
    fun updateSignupFakultas(value: String) { _signupFakultas.value = value }
    fun updateSignupPhone(value: String) { _signupPhone.value = value }
    fun updateSignupPassword(value: String) { _signupPassword.value = value }
    fun updateSignupConfirmPassword(value: String) { _signupConfirmPassword.value = value }
}