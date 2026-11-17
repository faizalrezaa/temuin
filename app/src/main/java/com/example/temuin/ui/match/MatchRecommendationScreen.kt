package com.example.temuin.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temuin.data.match.MatchAttribute
import com.example.temuin.data.match.MatchItem
import com.example.temuin.data.match.MatchLevel
import com.example.temuin.data.match.MatchResult
import com.example.temuin.data.match.MatchSource
import com.example.temuin.ui.theme.CardBackground
import com.example.temuin.ui.theme.OrangeLight
import com.example.temuin.ui.theme.OrangePrimary
import com.example.temuin.ui.theme.OrangePrimaryVariant
import com.example.temuin.ui.theme.TagHilangBg
import com.example.temuin.ui.theme.TagHilangText
import com.example.temuin.ui.theme.TagTemuanBg
import com.example.temuin.ui.theme.TagTemuanText
import com.example.temuin.ui.theme.TemuinTheme
import com.example.temuin.ui.theme.TextPrimary
import com.example.temuin.ui.theme.TextSecondary

/**
 * Stateless recommendation screen for Fungsi 4.
 * The caller supplies the state and navigation callbacks; no network or ViewModel lives here.
 */
@Composable
fun MatchRecommendationScreen(
    uiState: MatchUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onManualSearch: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onOpenReport: ((String) -> Unit)? = null,
    onLogin: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ScreenHeader(onBack = onBack)

        when (uiState) {
            MatchUiState.Loading -> CenteredState(Modifier.weight(1f)) {
                LoadingContent()
            }

            MatchUiState.Empty -> CenteredState(Modifier.weight(1f)) {
                EmptyContent(onManualSearch)
            }

            is MatchUiState.Error -> CenteredState(Modifier.weight(1f)) {
                ErrorContent(
                    failure = uiState.failure,
                    onRetry = onRetry,
                    onManualSearch = onManualSearch,
                    onLogin = onLogin,
                )
            }

            is MatchUiState.Success -> SuccessContent(
                result = uiState.result,
                modifier = Modifier.weight(1f),
                onOpenReport = onOpenReport,
            )
        }
    }
}

@Composable
private fun ScreenHeader(onBack: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (onBack != null) 8.dp else 20.dp,
                end = 20.dp,
                top = if (onBack != null) 12.dp else 20.dp,
                bottom = 8.dp,
            ),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                )
            }
        }
        Column(
            modifier = Modifier.padding(start = if (onBack != null) 12.dp else 0.dp),
        ) {
            Text(
                text = "Cari Kecocokan",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Kandidat laporan yang mungkin cocok dengan laporanmu.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SuccessContent(
    result: MatchResult,
    modifier: Modifier = Modifier,
    onOpenReport: ((String) -> Unit)? = null,
) {
    val banners = buildList {
        if (result.source == MatchSource.RULE_BASED && result.notices.isEmpty()) {
            add("AI tidak tersedia, jadi hasil ini disusun dari aturan. Tetap periksa manual.")
        }
        addAll(result.notices)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SummaryRow(result)
        }
        if (banners.isNotEmpty()) {
            item {
                InfoBanner(banners.joinToString(separator = "\n"))
            }
        }
        items(result.items, key = { it.report.id }) { item ->
            MatchItemCard(item, onOpenReport)
        }
        item {
            DisclaimerCard()
        }
    }
}

@Composable
private fun SummaryRow(result: MatchResult) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${result.items.size} kandidat",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.weight(1f))
        val (foreground, background) = if (result.source == MatchSource.AI) {
            TagTemuanText to TagTemuanBg
        } else {
            OrangePrimaryVariant to OrangeLight
        }
        Pill(
            text = MatchPresenter.sourceLabel(result.source),
            foreground = foreground,
            background = background,
        )
    }
}

@Composable
private fun MatchItemCard(item: MatchItem, onOpenReport: ((String) -> Unit)?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.report.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            append(MatchPresenter.categoryLabel(item.report.category))
                            append(" · ")
                            append(item.report.color ?: "Warna tidak dicatat")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(Modifier.width(8.dp))
                LevelPill(item.level)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "${item.report.locationName} · " +
                    MatchPresenter.formatOccurredAt(item.report.occurredAt),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            if (item.report.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (item.reason.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = item.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
            }

            if (item.incompleteData) {
                Spacer(Modifier.height(10.dp))
                Pill(
                    text = "Data belum lengkap",
                    foreground = OrangePrimaryVariant,
                    background = OrangeLight,
                )
            }

            AttributeGroup(
                title = "Cocok",
                attributes = item.matchedAttributes,
                foreground = TagTemuanText,
                background = TagTemuanBg,
            )
            AttributeGroup(
                title = "Berbeda",
                attributes = item.differentAttributes,
                foreground = TagHilangText,
                background = TagHilangBg,
            )
            AttributeGroup(
                title = "Tidak bisa dibandingkan",
                attributes = item.notComparableAttributes,
                foreground = TextSecondary,
                background = CardBackground,
            )

            if (!item.report.ownerName.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Pelapor: ${item.report.ownerName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            if (onOpenReport != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { onOpenReport(item.report.id) }) {
                    Text("Lihat Laporan")
                }
            }
        }
    }
}

@Composable
private fun LevelPill(level: MatchLevel) {
    val (foreground, background) = when (level) {
        MatchLevel.HIGH -> TagTemuanText to TagTemuanBg
        MatchLevel.MEDIUM -> OrangePrimaryVariant to OrangeLight
        MatchLevel.LOW -> TextSecondary to CardBackground
    }
    Pill(
        text = "Kecocokan ${MatchPresenter.levelLabel(level)}",
        foreground = foreground,
        background = background,
    )
}

@Composable
private fun AttributeGroup(
    title: String,
    attributes: List<MatchAttribute>,
    foreground: Color,
    background: Color,
) {
    if (attributes.isEmpty()) return

    Spacer(Modifier.height(10.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
    )
    Spacer(Modifier.height(4.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        attributes.forEach { attribute ->
            Pill(
                text = MatchPresenter.attributeLabel(attribute),
                foreground = foreground,
                background = background,
            )
        }
    }
}

@Composable
private fun Pill(text: String, foreground: Color, background: Color) {
    Surface(
        color = background,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
    }
}

@Composable
private fun InfoBanner(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Hasil ini hanya rekomendasi, bukan bukti kepemilikan. " +
                    "Kepemilikan tetap diverifikasi lewat proses klaim.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun CenteredState(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            content()
        }
    }
}

@Composable
private fun LoadingContent() {
    CircularProgressIndicator()
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Mencari kandidat…",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "Atribut laporan dibandingkan untuk menemukan kecocokan.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun EmptyContent(onManualSearch: () -> Unit) {
    Icon(
        imageVector = Icons.Default.SearchOff,
        contentDescription = null,
        tint = TextSecondary,
        modifier = Modifier.size(56.dp),
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = "Belum ada kandidat",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = "Tidak ada laporan aktif yang cocok. Coba cari manual lewat feed dan filter.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = onManualSearch) {
        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Cari Manual")
    }
}

@Composable
private fun ErrorContent(
    failure: MatchFailure,
    onRetry: () -> Unit,
    onManualSearch: () -> Unit,
    onLogin: (() -> Unit)?,
) {
    Icon(
        imageVector = Icons.Default.ErrorOutline,
        contentDescription = null,
        tint = OrangePrimary,
        modifier = Modifier.size(56.dp),
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = "Rekomendasi gagal dimuat",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(6.dp))
    Text(
        text = failure.message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )

    if (failure.requiresLogin && onLogin != null) {
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogin) { Text("Masuk Kembali") }
    }

    if (failure.canRetry) {
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Coba Lagi")
        }
    }

    if (failure.showManualSearch) {
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onManualSearch) {
            Text("Cari Manual")
        }
    }
}

@Preview(showBackground = true, name = "Rekomendasi - Sukses AI")
@Composable
private fun PreviewSuccess() {
    TemuinTheme {
        MatchRecommendationScreen(uiState = MatchUiState.Success(MatchFixtures.aiSuccess))
    }
}

@Preview(showBackground = true, name = "Rekomendasi - Sumber Aturan")
@Composable
private fun PreviewRuleBased() {
    TemuinTheme {
        MatchRecommendationScreen(uiState = MatchUiState.Success(MatchFixtures.ruleBased))
    }
}

@Preview(showBackground = true, name = "Rekomendasi - Data Kurang")
@Composable
private fun PreviewIncompleteData() {
    TemuinTheme {
        MatchRecommendationScreen(uiState = MatchUiState.Success(MatchFixtures.incomplete))
    }
}

@Preview(showBackground = true, name = "Rekomendasi - Kosong")
@Composable
private fun PreviewEmpty() {
    TemuinTheme {
        MatchRecommendationScreen(uiState = MatchUiState.Empty)
    }
}

@Preview(showBackground = true, name = "Rekomendasi - Loading")
@Composable
private fun PreviewLoading() {
    TemuinTheme {
        MatchRecommendationScreen(uiState = MatchUiState.Loading)
    }
}

@Preview(showBackground = true, name = "Rekomendasi - Gagal")
@Composable
private fun PreviewError() {
    TemuinTheme {
        MatchRecommendationScreen(uiState = MatchUiState.Error(MatchPresenter.failureFor(429)))
    }
}
