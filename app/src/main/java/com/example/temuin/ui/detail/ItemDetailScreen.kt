package com.example.temuin.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.temuin.data.model.Item
import com.example.temuin.data.model.ItemStatus
import com.example.temuin.ui.theme.*
import com.example.temuin.ui.components.AppPageTopBar

@Composable
fun ItemDetailScreen(
    viewModel: ItemDetailViewModel,
    onBackClick: () -> Unit,
    onFindMatches: () -> Unit,
) {
    val item by viewModel.item.collectAsState()
    val message by viewModel.message.collectAsState()
    val contactLink by viewModel.contactLink.collectAsState()
    val submitting by viewModel.submitting.collectAsState()
    val claimError by viewModel.claimError.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showClaimDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }
    LaunchedEffect(contactLink) {
        contactLink?.let {
            runCatching {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(it)))
            }
            viewModel.clearContactLink()
        }
    }

    Box(Modifier.fillMaxSize()) {
        ItemDetailContent(
            item = item,
            onBackClick = onBackClick,
            onContactClick = viewModel::contactReporter,
            onClaimClick = { viewModel.clearClaimError(); showClaimDialog = true },
            onFindMatches = onFindMatches
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))
    }

    if (showClaimDialog) {
        ClaimDialog(
            submitting = submitting,
            error = claimError,
            onDismiss = { showClaimDialog = false },
            onSubmit = { answer -> viewModel.submitClaim(answer) { showClaimDialog = false } }
        )
    }
}

@Composable
private fun ClaimDialog(
    submitting: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var answer by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Ajukan Klaim", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Sebutkan ciri khusus barang yang hanya diketahui pemiliknya. Jawaban ini rahasia dan hanya dibaca penemu.",
                    fontSize = 13.sp, color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    placeholder = { Text("Contoh: ada stiker kucing di belakang casing", color = TextSecondary.copy(alpha = 0.6f)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        val n = answer.trim().length
                        Text(if (n < 10) "Minimal 10 karakter ($n/10)" else "$n karakter")
                    }
                )
                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(error, color = Color(0xFFC62828), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(answer) },
                enabled = answer.trim().length >= 10 && !submitting,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (submitting) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (submitting) "Mengirim..." else "Kirim Klaim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailContent(
    item: Item?,
    onBackClick: () -> Unit,
    onContactClick: () -> Unit,
    onClaimClick: () -> Unit,
    onFindMatches: () -> Unit
) {
    Scaffold(
        topBar = { AppPageTopBar(title = item?.idLaporan ?: "Detail Laporan", onBackClick = onBackClick) },
        bottomBar = {
            if (item != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    color = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedButton(
                            onClick = onFindMatches,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, OrangePrimary)
                        ) {
                            Icon(Icons.Filled.Search, null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cari Kecocokan", color = OrangePrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        if (!item.isOwner) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (item.status == ItemStatus.DITEMUKAN) {
                                    // Figma: [Hubungi Pelapor] (outline) + [Verifikasi & Klaim] (oranye)
                                    OutlinedButton(
                                        onClick = onContactClick,
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, OrangePrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Hubungi Pelapor", color = OrangePrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                                    }
                                    val (claimText, claimEnabled) = when (item.myClaimStatus) {
                                        "PENDING" -> "Klaim Ditinjau" to false
                                        "APPROVED", "COMPLETED" -> "Klaim Disetujui" to false
                                        else -> "Verifikasi & Klaim" to item.canClaim
                                    }
                                    Button(
                                        onClick = onClaimClick,
                                        enabled = claimEnabled,
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.HowToReg, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(claimText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                                    }
                                } else {
                                    Button(
                                        onClick = onContactClick,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Hubungi Pelapor", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Memuat detail barang...", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item Image
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5ECE7)),
                    contentScale = ContentScale.Crop
                )

                // Status & Title Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailStatusTag(status = item.status)

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (item.status == ItemStatus.DITEMUKAN) Color(0xFF006E2A) else OrangePrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.time,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Description & Category Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Deskripsi & Kategori",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Kategori: ${item.kategori}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangePrimary
                        )

                        Text(
                            text = item.deskripsi,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Conditional Card based on Status
                if (item.status == ItemStatus.DITEMUKAN) {
                    // DITEMUKAN: Titik Pengambilan Resmi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F7EC)),
                        border = BorderStroke(1.dp, Color(0xFF006E2A).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Place,
                                    contentDescription = null,
                                    tint = Color(0xFF006E2A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Titik Pengambilan Resmi",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF006E2A)
                                )
                            }

                            Text(
                                text = item.titikPengambilan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            HorizontalDivider(color = Color(0xFF006E2A).copy(alpha = 0.2f))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Penemu: ${item.infoPelaporNama} (${item.infoPelaporFakultas})",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    // DICARI: Info Pemilik & Lokasi Terakhir
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8E5)),
                        border = BorderStroke(1.dp, Color(0xFFBA1A1A).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Place,
                                    contentDescription = null,
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Info Pemilik & Lokasi Terakhir",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFBA1A1A)
                                )
                            }

                            Text(
                                text = "Pelapor: ${item.infoPelaporNama} (${item.infoPelaporFakultas})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Text(
                                text = "Kontak: ${item.kontakPelapor}",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            HorizontalDivider(color = Color(0xFFBA1A1A).copy(alpha = 0.2f))

                            Text(
                                text = "Harap hubungi kontak di atas atau laporkan ke pos keamanan terdekat jika Anda menemukan barang ini.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DetailStatusTag(status: ItemStatus) {
    val bgColor = if (status == ItemStatus.DICARI) TagHilangBg else TagTemuanBg
    val textColor = if (status == ItemStatus.DICARI) TagHilangText else TagTemuanText
    val label = if (status == ItemStatus.DICARI) "DICARI" else "DITEMUKAN"
    val dotColor = textColor

    Surface(
        color = bgColor,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
