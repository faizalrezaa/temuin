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
import com.example.temuin.ui.theme.BackgroundLight
import com.example.temuin.ui.theme.TemuinTheme
import com.example.temuin.ui.theme.TextPrimary
import com.example.temuin.ui.theme.TextSecondary

private val fakultasList = listOf(
    "FIB UB", "FILKOM UB", "FT UB", "FEB UB", "FH UB", "FISIP UB", "FK UB", "FMIPA UB"
)

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    var nama by remember { mutableStateOf("Alya Rahmadina") }
    var nim by remember { mutableStateOf("2106728910") }
    var fakultas by remember { mutableStateOf("FIB UB") }
    var telepon by remember { mutableStateOf("0812-3456-7890") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val passwordError = password.isNotEmpty() && confirm.isNotEmpty() && password != confirm

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
            Box(Modifier.clickable { /* TODO: pilih foto */ }) {
                ProfileAvatar(92.dp)
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

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                FieldLabel("Nama Lengkap")
                ProfileTextField(nama, { nama = it })

                FieldLabel("NIM (Nomor Induk Mahasiswa)")
                ProfileTextField(nim, { nim = it }, keyboardType = KeyboardType.Number)
                Hint(Icons.Outlined.Info, "Digunakan untuk validasi kartu identitas kampus")

                FieldLabel("Fakultas")
                FakultasDropdown(fakultas) { fakultas = it }

                FieldLabel("Nomor Telepon / WhatsApp")
                ProfileTextField(telepon, { telepon = it }, keyboardType = KeyboardType.Phone)
                Hint(Icons.AutoMirrored.Outlined.Chat, "Untuk koordinasi instan saat klaim barang")

                HorizontalDivider(Modifier.padding(vertical = 20.dp), color = Color(0xFFF0E4DF))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LockReset, null, tint = ProfileBrown, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pengaturan Keamanan", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }

                FieldLabel("Kata Sandi Baru")
                PasswordField(password, { password = it }, "Kosongkan jika tidak diubah")
                Hint(Icons.Outlined.Shield, "Gunakan minimal 8 karakter dengan kombinasi angka")

                FieldLabel("Ulangi Kata Sandi")
                PasswordField(confirm, { confirm = it }, "Ulang kata sandi baru", isError = passwordError)
                if (passwordError) {
                    Text("Kata sandi tidak sama", color = ProfileDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onSave,
            enabled = !passwordError,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileOrange)
        ) {
            Text("Simpan Perubahan", fontSize = 15.sp)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Batal / Kembali", color = Color(0xFF5D4037), fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
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
private fun FakultasDropdown(selected: String, onSelect: (String) -> Unit) {
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
            fakultasList.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onSelect(it); expanded = false })
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun EditProfileScreenPreview() {
    TemuinTheme { EditProfileScreen(onBack = {}, onSave = {}) }
}
