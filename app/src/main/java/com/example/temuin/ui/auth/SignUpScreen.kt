package com.example.temuin.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val name by viewModel.signupName.collectAsState()
    val nim by viewModel.signupNim.collectAsState()
    val phone by viewModel.signupPhone.collectAsState()
    val password by viewModel.signupPassword.collectAsState()
    val confirmPassword by viewModel.signupConfirmPassword.collectAsState()
    val fakultas by viewModel.signupFakultas.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }
    var expandedFakultas by remember { mutableStateOf(false) }
    val listFakultas = listOf("Fakultas Ilmu Komputer", "Fakultas Teknik", "Fakultas Ekonomi", "Fakultas Hukum")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Daftar Akun Temuin",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Lengkapi data sivitas akademika untuk verifikasi\nakun pencarian & pelaporan barang.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Info Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Akun mahasiswa memerlukan NIM dan Fakultas yang valid untuk verifikasi kepemilikan barang.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Form Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Field: Nama Lengkap
                LabelWithStar("Nama Lengkap")
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::updateSignupName,
                    placeholder = { Text("cth. Dimas Pratama", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field: NIM
                LabelWithStar("NIM (Nomor Induk Mahasiswa)")
                OutlinedTextField(
                    value = nim,
                    onValueChange = viewModel::updateSignupNim,
                    placeholder = { Text("cth. 2106728910", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                HelperText("Digunakan untuk validasi kartu KTM")

                Spacer(modifier = Modifier.height(16.dp))

                // Field: Fakultas (Dropdown)
                LabelWithStar("Fakultas")
                ExposedDropdownMenuBox(
                    expanded = expandedFakultas,
                    onExpandedChange = { expandedFakultas = !expandedFakultas }
                ) {
                    OutlinedTextField(
                        value = if (fakultas.isEmpty()) "Pilih Fakultas Anda" else fakultas,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFakultas) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = if (fakultas.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFakultas,
                        onDismissRequest = { expandedFakultas = false }
                    ) {
                        listFakultas.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    viewModel.updateSignupFakultas(selectionOption)
                                    expandedFakultas = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Field: Telepon
                LabelWithStar("Nomor Telepon / WhatsApp")
                OutlinedTextField(
                    value = phone,
                    onValueChange = viewModel::updateSignupPhone,
                    placeholder = { Text("812-3456-7890", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                HelperText("Untuk koordinasi instan saat klaim barang", isGreen = true)

                Spacer(modifier = Modifier.height(16.dp))

                // Field: Password
                LabelWithStar("Kata Sandi")
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::updateSignupPassword,
                    placeholder = { Text("Minimal 8 karakter", color = Color.Gray) },
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
                HelperText("Gunakan minimal 8 karakter")

                Spacer(modifier = Modifier.height(16.dp))

                // Field: Confirm Password
                LabelWithStar("Ulangi Kata Sandi")
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = viewModel::updateSignupConfirmPassword,
                    placeholder = { Text("Masukkan ulang kata sandi", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Checkbox Terms
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it }
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Saya menyetujui ")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                                append("Ketentuan Layanan")
                            }
                            append(" & ")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                                append("Kebijakan Privasi")
                            }
                            append(" Komunitas Temuin.")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSignupSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Buat Akun Temuin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White // Menambahkan warna putih eksplisit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White // Menambahkan warna putih eksplisit pada ikon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Text
        Row(modifier = Modifier.clickable { onNavigateToLogin() }) {
            Text("Sudah punya akun? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Masuk di sini >",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Trust Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9)) // Light green background
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF2E7D32), // Dark green icon
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Terintegrasi sistem verifikasi identitas kampus terpercaya.",
                style = MaterialTheme.typography.labelMedium,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun LabelWithStar(text: String) {
    Text(
        text = buildAnnotatedString {
            append(text)
            withStyle(style = SpanStyle(color = Color.Red)) {
                append(" *")
            }
        },
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun HelperText(text: String, isGreen: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = if (isGreen) Color(0xFF2E7D32) else Color.Gray,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
    )
}