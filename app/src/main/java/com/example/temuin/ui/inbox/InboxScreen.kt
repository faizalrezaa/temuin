package com.example.temuin.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temuin.ui.components.ReportThumbnail
import com.example.temuin.data.model.InboxFilter
import com.example.temuin.data.model.InboxItem
import com.example.temuin.data.model.StatusLaporanType
import com.example.temuin.ui.theme.*

// ── Local design tokens ──────────────────────────────────────────────────────
private val CardBorderColor   = Color(0xFFE1BFB4).copy(alpha = 0.5f)
private val TabInactiveBg     = Color(0xFFF5ECE7)
private val TipsBg            = Color(0xFFFBF2ED)
private val TipsText          = Color(0xFF594139)

// Status badge colors
private val BadgeOrangeBg     = Color(0xFFFEF6EC)
private val BadgeOrangeText   = Color(0xFFF2994A)
private val BadgeGreenBg      = Color(0xFFE6F4EA)
private val BadgeGreenText    = Color(0xFF1E8E3E)
private val BadgeRedBg        = Color(0xFFFCE8E6)
private val BadgeRedText      = Color(0xFFD93025)

// ── Stateful Screen ──────────────────────────────────────────────────────────
@Composable
fun InboxScreen(viewModel: InboxViewModel) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val filteredItems  by viewModel.filteredItems.collectAsState()
    val loadingIds     by viewModel.loadingIds.collectAsState()
    val counts         by viewModel.counts.collectAsState()
    val actionResult   by viewModel.actionResult.collectAsState()
    val chatLink       by viewModel.chatLink.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(chatLink) {
        chatLink?.let {
            runCatching {
                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(it)))
            }.onFailure { snackbarHostState.showSnackbar("Tidak dapat membuka WhatsApp") }
            viewModel.clearChatLink()
        }
    }

    LaunchedEffect(actionResult) {
        actionResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionResult()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        InboxContent(
            selectedFilter = selectedFilter,
            filteredItems  = filteredItems,
            counts         = counts,
            loadingIds     = loadingIds,
            onFilterSelect = viewModel::selectFilter,
            onSetujui      = viewModel::setujuiKlaim,
            onChat         = viewModel::bukaChat,
            onTolak        = viewModel::tolakKlaim
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Stateless Content ────────────────────────────────────────────────────────
@Composable
fun InboxContent(
    selectedFilter: InboxFilter,
    filteredItems:  List<InboxItem>,
    counts:         Map<InboxFilter, Int>,
    loadingIds:     Set<String>,
    onFilterSelect: (InboxFilter) -> Unit,
    onSetujui:      (String) -> Unit,
    onChat:         (String) -> Unit = {},
    onTolak:        (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        InboxTabRow(
            selectedFilter = selectedFilter,
            counts         = counts,
            onSelect       = onFilterSelect
        )

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredItems.isEmpty()) {
                item { EmptyInbox(selectedFilter) }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    when (item) {
                        is InboxItem.PermintaanKlaim ->
                            PermintaanKlaimCard(
                                item       = item,
                                isLoading  = item.id in loadingIds,
                                onSetujui  = { onSetujui(item.id) },
                                onTolak    = { onTolak(item.id) }
                            )
                        is InboxItem.StatusLaporan ->
                            StatusLaporanCard(item = item, onChat = { item.reportId?.let(onChat) })
                    }
                }
            }
        }
    }
}

// ── Tab Row ──────────────────────────────────────────────────────────────────
@Composable
fun InboxTabRow(
    selectedFilter: InboxFilter,
    counts:         Map<InboxFilter, Int>,
    onSelect:       (InboxFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InboxFilter.entries.forEach { filter ->
            val selected = selectedFilter == filter
            val count    = counts[filter] ?: 0

            Surface(
                onClick       = { onSelect(filter) },
                shape         = RoundedCornerShape(50),
                color         = if (selected) OrangePrimary else TabInactiveBg,
                modifier      = Modifier.wrapContentWidth()
            ) {
                Text(
                    text       = if (count > 0) "${filter.label} ($count)" else filter.label,
                    color      = if (selected) Color.White else TextSecondary,
                    fontSize   = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    maxLines   = 1
                )
            }
        }
    }
}

// ── Permintaan Klaim Card ────────────────────────────────────────────────────
@Composable
fun PermintaanKlaimCard(
    item:      InboxItem.PermintaanKlaim,
    isLoading: Boolean,
    onSetujui: () -> Unit,
    onTolak:   () -> Unit
) {
    InboxCard {
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ItemThumbnail(url = item.imageUrl, title = item.judulBarang)

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InboxStatusBadge(
                    label   = "MENUNGGU PERSETUJUAN",
                    bgColor = BadgeOrangeBg,
                    textColor = BadgeOrangeText
                )

                Text(
                    text       = item.judulBarang,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Text(
                    text     = "Diklaim oleh ${item.namaPengklaim}",
                    fontSize = 11.sp,
                    color    = TextSecondary
                )

                Text(
                    text     = item.waktu,
                    fontSize = 10.sp,
                    color    = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Jawaban verifikasi
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text       = "Jawaban Verifikasi",
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Text(
                text     = item.jawabanVerifikasi,
                fontSize = 12.sp,
                color    = TextSecondary,
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tips banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TipsBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint     = BadgeOrangeText,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text     = item.tipsLabel,
                fontSize = 11.sp,
                color    = TipsText,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action buttons
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color    = OrangePrimary,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                TextButton(
                    onClick  = onTolak,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tolak", color = TextSecondary, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick  = onSetujui,
                    modifier = Modifier.weight(2f),
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Setujui Klaim", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Status Laporan Card ───────────────────────────────────────────────────────
@Composable
fun StatusLaporanCard(item: InboxItem.StatusLaporan, onChat: () -> Unit = {}) {
    val (badgeBg, badgeText, badgeLabel) = when (item.status) {
        StatusLaporanType.MENUNGGU_VERIFIKASI -> Triple(BadgeOrangeBg, BadgeOrangeText, "MENUNGGU VERIFIKASI PENEMU")
        StatusLaporanType.DISETUJUI_PENEMU -> Triple(BadgeGreenBg, BadgeGreenText, "DISETUJUI PENEMU • SIAP DIAMBIL")
        StatusLaporanType.KLAIM_DITOLAK    -> Triple(BadgeRedBg,   BadgeRedText,   "KLAIM DITOLAK")
        StatusLaporanType.TITIP_DI_POS     -> Triple(BadgeOrangeBg, BadgeOrangeText, "TITIP DI POS • MENUNGGU KLAIM")
    }

    InboxCard {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ItemThumbnail(url = item.imageUrl, title = item.judulBarang)

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InboxStatusBadge(
                    label     = badgeLabel,
                    bgColor   = badgeBg,
                    textColor = badgeText
                )

                Text(
                    text       = item.judulBarang,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Text(
                    text       = item.pesan,
                    fontSize   = 11.sp,
                    color      = TextSecondary,
                    lineHeight = 15.sp
                )

                item.nomorReferensi?.let { ref ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("No. Ref:", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text       = ref,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color      = OrangePrimary
                        )
                    }
                }

                Text(
                    text     = item.waktu,
                    fontSize = 10.sp,
                    color    = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }

        if (item.canChat && item.reportId != null) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = onChat,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape    = RoundedCornerShape(50),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Chat Penemu via WhatsApp", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Shared Composables ───────────────────────────────────────────────────────
@Composable
fun InboxCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, CardBorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        content = content
    )
}

@Composable
fun ItemThumbnail(url: String, title: String) {
    ReportThumbnail(url = url, title = title, modifier = Modifier.size(80.dp))
}

@Composable
fun InboxStatusBadge(label: String, bgColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .background(bgColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(5.dp).background(textColor, CircleShape))
        Text(
            text       = label,
            color      = textColor,
            fontSize   = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun EmptyInbox(filter: InboxFilter) {
    val message = when (filter) {
        InboxFilter.PERMINTAAN_KLAIM -> "Belum ada permintaan klaim"
        InboxFilter.STATUS_LAPORAN   -> "Belum ada update status laporan"
        else                         -> "Inbox kosong"
    }

    Column(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 72.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Inbox,
            contentDescription = null,
            tint     = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
        )
        Text(message, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text("Semua beres di sini 🎉", fontSize = 12.sp, color = TextSecondary)
    }
}
