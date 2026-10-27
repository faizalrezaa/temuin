package com.example.temuin.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SubmissionScreen(viewModel: AuthViewModel, onBackToForm: () -> Unit) {
    val result by viewModel.submission.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hasil Pengolahan Form",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        result?.let { submission ->
            Text(submission.title, style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Identitas", fontWeight = FontWeight.SemiBold)
                    Text(submission.combinedIdentity)
                    Text("Kontak", fontWeight = FontWeight.SemiBold)
                    Text(submission.contact)
                }
            }
            Text(submission.source)
        } ?: Text("Belum ada hasil. Isi dan kirim formulir terlebih dahulu.")
        Text(
            text = "Demo Tugas 2: validasi dan pengolahan lokal. " +
                "Belum membuat akun atau memverifikasi identitas kampus. " +
                "Kata sandi tidak disertakan dalam hasil.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBackToForm, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Form")
        }
    }
}
