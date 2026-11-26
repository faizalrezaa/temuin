package com.example.temuin.ui.match

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.temuin.data.model.Item
import com.example.temuin.data.model.ItemStatus
import com.example.temuin.ui.detail.ItemDetailContent
import com.example.temuin.ui.theme.TemuinTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MatchFlowTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun ownerCanFindMatchesWithoutClaimingTheirOwnReport() {
        var requested = false
        val item = Item(
            id = "own-report", idLaporan = "own-report", title = "Dompet",
            kategori = "Dompet", location = "FILKOM", time = "Hari ini",
            status = ItemStatus.DITEMUKAN, deskripsi = "Dompet hitam",
            titikPengambilan = "Pos", infoPelaporNama = "Pelapor",
            infoPelaporFakultas = "FILKOM", kontakPelapor = "", isOwner = true,
        )
        compose.setContent {
            TemuinTheme {
                ItemDetailContent(item, {}, {}, {}, onFindMatches = { requested = true })
            }
        }
        compose.onNodeWithText("Cari Kecocokan").performClick()
        compose.runOnIdle { assertTrue(requested) }
        compose.onNodeWithText("Verifikasi & Klaim").assertDoesNotExist()
        compose.onNodeWithText("Hubungi Pelapor").assertDoesNotExist()
    }

    @Test
    fun candidateOpensItsReport() {
        var selectedId: String? = null
        val result = MatchFixtures.aiSuccess.copy(items = MatchFixtures.aiSuccess.items.take(1))
        compose.setContent {
            TemuinTheme {
                MatchRecommendationScreen(
                    MatchUiState.Success(result),
                    onOpenReport = { selectedId = it },
                )
            }
        }
        compose.onNodeWithText(result.items.first().report.title).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(result.items.first().report.id, selectedId) }
    }

    @Test
    fun expiredSessionHasActionableLogin() {
        var loginRequested = false
        compose.setContent {
            TemuinTheme {
                MatchRecommendationScreen(
                    MatchUiState.Error(MatchPresenter.failureFor(401)),
                    onLogin = { loginRequested = true },
                )
            }
        }
        compose.onNodeWithText("Masuk Kembali").assertIsDisplayed().performClick()
        compose.runOnIdle { assertTrue(loginRequested) }
        compose.onNodeWithText("Coba Lagi").assertDoesNotExist()
    }
}
