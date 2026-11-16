package com.example.temuin.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temuin.ui.theme.BackgroundLight
import com.example.temuin.ui.theme.TemuinTheme
import com.example.temuin.ui.theme.TextPrimary
import com.example.temuin.ui.theme.TextSecondary

data class LaporanItem(
    val title: String,
    val location: String,
    val claimCount: Int,
    val done: Boolean = false
)

private val dummyLaporan = listOf(
    LaporanItem("KTM UB a.n. Dimas Pratama", "Pos Perpus Pusat", claimCount = 1),
    LaporanItem("Earbuds TWS Soundcore Hitam", "Piket RIK 2", claimCount = 0),
)

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val laporan = if (selectedTab == 0) dummyLaporan.filter { !it.done } else dummyLaporan.filter { it.done }

    run {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Kartu profil
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onEditProfile),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(64.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Alya Rahmadina", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("2106728910 • FIB UB", fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        StatusChip("Profil Mahasiswa", dotColor = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text("Laporan Saya", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(10.dp))

            // Segmented tab
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(ProfileChipBg)
                    .padding(4.dp)
            ) {
                listOf("Aktif (2)", "Selesai / Terambil (3)").forEachIndexed { i, label ->
                    val sel = selectedTab == i
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) ProfileOrange else Color.Transparent)
                            .clickable { selectedTab = i }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 14.sp, color = if (sel) Color.White else TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            if (laporan.isEmpty()) {
                Text(
                    "Belum ada laporan.",
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
            laporan.forEach {
                LaporanCard(it)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(16.dp))
            LogoutCard(onLogout)
        }
    }
}

@Composable
private fun StatusChip(text: String, dotColor: Color, bg: Color = ProfileChipBg) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
private fun LaporanCard(item: LaporanItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                // Placeholder gambar barang
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ProfileChipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, null, tint = TextSecondary)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "BARANG DITEMUKAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileBrownText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(Modifier.width(2.dp))
                        Text(item.location, fontSize = 13.sp, color = TextSecondary)
                    }
                }
                Icon(Icons.Default.MoreVert, "Opsi", tint = TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            if (item.claimCount > 0) {
                StatusChip(
                    "${item.claimCount} Permintaan Verifikasi Masuk",
                    dotColor = ProfileOrange,
                    bg = ProfileDangerBg
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { /* TODO: verifikasi klaim */ },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfileBrown)
                ) {
                    Icon(Icons.Outlined.VerifiedUser, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Verifikasi Klaim")
                }
            } else {
                StatusChip("Belum ada klaim", dotColor = TextSecondary)
            }
        }
    }
}

@Composable
private fun LogoutCard(onLogout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onLogout),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(ProfileDangerBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ProfileDanger, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Keluar Akun", fontWeight = FontWeight.SemiBold, color = ProfileDanger, fontSize = 15.sp)
                Text("Akun Temuin", fontSize = 13.sp, color = TextSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = ProfileDanger)
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ProfileScreenPreview() {
    TemuinTheme { ProfileScreen(onEditProfile = {}, onLogout = {}) }
}
