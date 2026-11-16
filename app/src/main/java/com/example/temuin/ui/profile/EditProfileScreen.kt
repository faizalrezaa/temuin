package com.example.temuin.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temuin.ui.theme.BackgroundLight
import com.example.temuin.ui.theme.TemuinTheme
import com.example.temuin.ui.theme.TextPrimary
import com.example.temuin.ui.theme.TextSecondary

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: EditProfileViewModel = viewModel(),
) {
    val s by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) viewModel.uploadAvatar(bytes, mime)
        }
    }

    LaunchedEffect(s.saved) { if (s.saved) onSave() }
    LaunchedEffect(s.error) {
        s.error?.let { snackbar.showSnackbar(it); viewModel.dismissError() }
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Box(
            Modifier
                .padding(top = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFE6E2))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = TextPrimary)
        }

        // Foto profil
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.clickable(enabled = !s.uploadingAvatar) {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                ProfileAvatar(92.dp, s.avatarUrl)
                if (s.uploadingAvatar) {
                    CircularProgressIndicator(Modifier.size(92.dp), color = ProfileOrange, strokeWidth = 3.dp)
                }
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ProfileOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, "Ubah foto", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Ketuk untuk mengubah foto profil (maks. 5MB)", fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(Modifier.height(24.dp))

        if (s.loading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ProfileOrange)
            }
        } else Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                FieldLabel("Nama Lengkap")
                ProfileTextField(s.name, viewModel::onName)

                FieldLabel("NIM (Nomor Induk Mahasiswa)")
                ProfileTextField(s.nim, viewModel::onNim, keyboardType = KeyboardType.Number)
                Hint(Icons.Outlined.Info, "Digunakan untuk validasi kartu identitas kampus")

                FieldLabel("Fakultas")
                FakultasDropdown(s.faculty, s.faculties.map { it.name }, viewModel::onFaculty)

                FieldLabel("Nomor Telepon / WhatsApp")
                ProfileTextField(s.whatsapp, viewModel::onWhatsapp, keyboardType = KeyboardType.Phone)
                Hint(Icons.AutoMirrored.Outlined.Chat, "Untuk koordinasi instan saat klaim barang")

                HorizontalDivider(Modifier.padding(vertical = 20.dp), color = Color(0xFFF0E4DF))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LockReset, null, tint = ProfileBrown, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pengaturan Keamanan", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }

                FieldLabel("Kata Sandi Baru")
                PasswordField(s.newPassword, viewModel::onNewPassword, "Kosongkan jika tidak diubah")
                Hint(Icons.Outlined.Shield, "Gunakan minimal 8 karakter dengan kombinasi angka")

                FieldLabel("Ulangi Kata Sandi")
                PasswordField(s.confirmPassword, viewModel::onConfirmPassword, "Ulang kata sandi baru", isError = s.passwordMismatch)
                if (s.passwordMismatch) {
                    Text("Kata sandi tidak sama", color = ProfileDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                // API butuh current_password untuk ganti sandi → muncul hanya kalau sandi baru diisi
                if (s.wantsPasswordChange) {
                    FieldLabel("Kata Sandi Saat Ini")
                    PasswordField(s.currentPassword, viewModel::onCurrentPassword, "Masukkan kata sandi saat ini")
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = viewModel::save,
            enabled = !s.passwordMismatch && !s.saving && !s.loading && !s.uploadingAvatar,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileOrange)
        ) {
            if (s.saving) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Simpan Perubahan", fontSize = 15.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Batal / Kembali", color = Color(0xFF5D4037), fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun Hint(icon: ImageVector, text: String) {
    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(14.dp), tint = TextSecondary)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = ProfileFieldBorder,
    focusedBorderColor = ProfileOrange,
    unfocusedContainerColor = Color.White,
    focusedContainerColor = Color.White,
)

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = fieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        isError = isError,
        colors = fieldColors(),
        placeholder = { Text(placeholder, color = TextSecondary) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (visible) "Sembunyikan" else "Tampilkan",
                    tint = TextPrimary
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FakultasDropdown(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = TextPrimary) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false })
            }
        }
    }
}

