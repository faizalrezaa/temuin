package com.example.temuin.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.loginUiState.collectAsStateWithLifecycle()
    val nipNim = state.identifier
    val password = state.password
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Memastikan seluruh elemen berada tepat di tengah layar
    ) {
        Text(
            text = "Masuk ke Akun Temuin",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Demo pemeriksaan format NIM/NIP/email UB dan kata sandi. Belum terhubung ke layanan autentikasi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                LabelWithStar("NIP/NIM/Email Kampus")
                OutlinedTextField(
                    value = nipNim,
                    onValueChange = viewModel::updateLoginNipNim,
                    isError = state.identifierError != null,
                    supportingText = { FormFieldError(state.identifierError) },
                    placeholder = { Text("cth. 245150200111066", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                LabelWithStar("Kata Sandi")
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::updateLoginPassword,
                    isError = state.passwordError != null,
                    supportingText = { FormFieldError(state.passwordError) },
                    placeholder = { Text("Masukkan kata sandi", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (viewModel.submitLogin()) onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Masuk Sekarang",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White // Menambahkan warna putih eksplisit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White // Menambahkan warna putih eksplisit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.clickable { onNavigateToSignup() }) {
            Text("Belum punya akun? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Daftar Sekarang",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}