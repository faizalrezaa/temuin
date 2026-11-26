package com.example.temuin.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.temuin.ui.components.AppPageTopBar
import com.example.temuin.ui.components.ReportThumbnail
import com.example.temuin.ui.components.TemuinReportCard
import com.example.temuin.ui.theme.BackgroundLight
import com.example.temuin.ui.theme.OrangePrimary
import com.example.temuin.ui.theme.TextPrimary
import com.example.temuin.ui.theme.TextSecondary

@Composable
fun HelpScreen(
    viewModel: HelpViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenReport: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.sending) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight).imePadding(),
    ) {
        AppPageTopBar(title = "Bantuan Temuin", onBackClick = onBack)
        Text(
            text = "Pilih topik atau tanyakan cara memakai Temuin.",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.topics, key = { it.id }) { topic ->
                FilterChip(
                    selected = false,
                    onClick = { viewModel.askTopic(topic) },
                    label = { Text(topic.title) },
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.messages.isEmpty()) {
                item {
                    AssistantBubble(
                        text = buildString {
                            append("Halo")
                            state.firstName?.let { append(", $it") }
                            append("! Ada barang yang sedang kamu cari? Aku bisa memandu cara membuat dan mencari laporan, mencari laporan aktif yang mungkin cocok, serta memahami peta, penitipan, dan status klaim di Inbox. Mau mulai dari mana?")
                        },
                        source = "Bantuan Temuin",
                    )
                }
            }
            items(state.messages, key = { it.id }) { message ->
                if (message.role == "user") {
                    UserBubble(message.text)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AssistantBubble(
                            text = message.text,
                            source = when (message.source) {
                                "DEEPSEEK" -> "Bantuan Temuin"
                                "DATABASE" -> "Laporan Temuin"
                                else -> "Panduan Temuin"
                            },
                        )
                        val topic = state.topics.firstOrNull { it.id == message.topic }
                        if (topic != null) {
                            TextButton(onClick = { onNavigate(topic.id) }) {
                                Text("Buka ${destinationLabel(topic.id)}", color = OrangePrimary)
                            }
                        }
                        if (message.reports.isNotEmpty()) {
                            Text("Laporan terkait", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            message.reports.forEach { report ->
                                HelpReportCard(report = report, onClick = { onOpenReport(report.id) })
                            }
                        }
                    }
                }
            }
            if (state.sending) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = OrangePrimary)
                        Text("Menyiapkan jawaban…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        state.error?.let { error ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(error, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                TextButton(onClick = viewModel::retry) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("Coba lagi")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { if (it.codePointCount(0, it.length) <= MAX_HELP_MESSAGE_LENGTH) draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tulis pertanyaan…") },
                supportingText = { Text("${draft.codePointCount(0, draft.length)}/$MAX_HELP_MESSAGE_LENGTH") },
                maxLines = 3,
                enabled = !state.sending && state.error == null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (draft.isNotBlank() && !state.sending && state.error == null) {
                        viewModel.send(draft)
                        draft = ""
                    }
                }),
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = {
                    viewModel.send(draft)
                    draft = ""
                },
                enabled = draft.isNotBlank() && !state.sending && state.error == null,
                modifier = Modifier.padding(bottom = 18.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim pertanyaan", tint = OrangePrimary)
            }
        }
    }
}

@Composable
private fun HelpReportCard(
    report: com.example.temuin.data.help.HelpReportReference,
    onClick: () -> Unit,
) {
    val typeLabel = if (report.type == "FOUND") "Ditemukan" else "Dicari"
    val metadata = listOfNotNull(
        report.color?.takeIf(String::isNotBlank),
        report.locationName.takeIf(String::isNotBlank),
    ).joinToString(" · ")

    TemuinReportCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ReportThumbnail(
                url = report.photoUrl,
                title = report.title,
                modifier = Modifier.size(68.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(typeLabel, style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
                Text(
                    report.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (metadata.isNotBlank()) {
                    Text(
                        metadata,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text("Buka detail", style = MaterialTheme.typography.labelMedium, color = OrangePrimary)
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = OrangePrimary,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
        ) {
            Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), color = Color.White)
        }
    }
}

@Composable
private fun AssistantBubble(text: String, source: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(source, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(text, color = TextPrimary)
            }
        }
    }
}

private fun destinationLabel(id: String) = when (id) {
    "REPORTING" -> "Lapor"
    "SEARCH" -> "Beranda"
    "MAP_CUSTODY" -> "Peta"
    "CLAIM_INBOX" -> "Inbox"
    else -> "Bantuan"
}
