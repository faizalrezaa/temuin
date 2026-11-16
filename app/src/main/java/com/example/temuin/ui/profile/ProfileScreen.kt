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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.temuin.data.remote.ReportDto
import com.example.temuin.ui.theme.BackgroundLight
import com.example.temuin.ui.theme.TextPrimary
import com.example.temuin.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onOpenReport: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Muat ulang setiap kali halaman dibuka (mis. setelah Edit Profil)
    LaunchedEffect(Unit) { viewModel.load() }

    val laporan = if (selectedTab == 0) state.activeReports else state.doneReports

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onEditProfile),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(64.dp, state.user?.avatarUrl)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    val user = state.user
                    if (user == null && state.loading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = ProfileOrange)
                    } else {
                        Text(user?.name ?: "-", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        val sub = listOfNotNull(user?.nim, user?.faculty).filter { it.isNotBlank() }
                        if (sub.isNotEmpty()) {
                            Text(sub.joinToString(" • "), fontSize = 13.sp, color = TextSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        StatusChip("Profil Mahasiswa", dotColor = Color(0xFF2E7D32))
                    }
                }
            }
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            ErrorBanner(it, onRetry = viewModel::load)
        }

        Spacer(Modifier.height(28.dp))
        Text("Laporan Saya", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(10.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(ProfileChipBg)
                .padding(4.dp)
        ) {
            listOf(
                "Aktif (${state.activeReports.size})",
                "Selesai / Terambil (${state.doneReports.size})"
            ).forEachIndexed { i, label ->
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
        when {
            state.loading && laporan.isEmpty() -> Box(
                Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = ProfileOrange) }

            laporan.isEmpty() -> Text(
                if (selectedTab == 0) "Belum ada laporan aktif." else "Belum ada laporan yang selesai.",
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            )

            else -> laporan.forEach {
                LaporanCard(item = it, onClick = { onOpenReport(it.id) })
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        LogoutCard {
            viewModel.logout()
            onLogout()
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ProfileDangerBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, color = ProfileDanger, fontSize = 13.sp, modifier = Modifier.weight(1f))
        TextButton(onClick = onRetry) { Text("Coba lagi", color = ProfileDanger) }
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

private fun ReportDto.typeLabel() = if (type == "LOST") "BARANG HILANG" else "BARANG DITEMUKAN"

private fun ReportDto.statusLabel(): String? = when (status) {
    "COMPLETED" -> "Selesai / Terambil"
    "CLOSED" -> "Ditutup"
    else -> when (custody) {
        "AWAITING_DROPOFF" -> "Menunggu Penitipan"
        "DEPOSITED" -> "Dititipkan"
        "UNAVAILABLE" -> "Tidak tersedia"
        else -> null
    }
}

@Composable
private fun LaporanCard(item: ReportDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ProfileChipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, null, tint = TextSecondary)
                    if (!item.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.photoUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.typeLabel(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.type == "LOST") ProfileDanger else ProfileBrownText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        item.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    val location = item.pickupPoint?.name ?: item.locationName
                    if (!location.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, Modifier.size(14.dp), tint = TextSecondary)
                            Spacer(Modifier.width(2.dp))
                            Text(location, fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Icon(Icons.Default.MoreVert, "Opsi", tint = TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            val statusLabel = item.statusLabel()
            when {
                statusLabel != null -> StatusChip(statusLabel, dotColor = TextSecondary)
                item.type == "FOUND" -> StatusChip("Belum ada klaim", dotColor = TextSecondary)
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
