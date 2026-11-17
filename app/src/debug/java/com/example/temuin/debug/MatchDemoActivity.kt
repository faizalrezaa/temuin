package com.example.temuin.debug

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.temuin.ui.match.MatchFixtures
import com.example.temuin.ui.match.MatchPresenter
import com.example.temuin.ui.match.MatchRecommendationScreen
import com.example.temuin.ui.match.MatchUiState
import com.example.temuin.ui.theme.TemuinTheme
import kotlinx.coroutines.delay

/**
 * Debug-only launcher for demonstrating Fungsi 4 without auth, backend, or navigation.
 * This activity is declared only in src/debug and never ships in a release build.
 */
class MatchDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemuinTheme {
                MatchDemoContent()
            }
        }
    }
}

@Composable
private fun MatchDemoContent() {
    var selected by remember { mutableStateOf(DemoState.AI) }
    val context = LocalContext.current

    LaunchedEffect(selected) {
        if (selected == DemoState.LOADING) {
            delay(1_200)
            selected = DemoState.AI
        }
    }

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Text("Demo UI · data simulasi", modifier = Modifier.padding(horizontal = 12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DemoState.entries.forEach { demo ->
                FilterChip(
                    selected = selected == demo,
                    onClick = { selected = demo },
                    label = { Text(demo.label) },
                )
            }
        }

        MatchRecommendationScreen(
            uiState = selected.state,
            modifier = Modifier.weight(1f),
            onRetry = { selected = DemoState.AI },
            onManualSearch = {
                Toast.makeText(
                    context,
                    "Pencarian manual belum terhubung di demo ini",
                    Toast.LENGTH_SHORT,
                ).show()
            },
        )
    }
}

private enum class DemoState(val label: String, val state: MatchUiState) {
    AI("AI", MatchUiState.Success(MatchFixtures.aiSuccess)),
    RULE_BASED("Aturan", MatchUiState.Success(MatchFixtures.ruleBased)),
    INCOMPLETE("Data kurang", MatchUiState.Success(MatchFixtures.incomplete)),
    EMPTY("Kosong", MatchUiState.Empty),
    LOADING("Loading", MatchUiState.Loading),
    RATE_LIMITED("Gagal 429", MatchUiState.Error(MatchPresenter.failureFor(429))),
}
