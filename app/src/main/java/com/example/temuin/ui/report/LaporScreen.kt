package com.example.temuin.ui.report

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.temuin.data.model.LaporanFormData
import com.example.temuin.data.model.MetodePenyerahan
import com.example.temuin.data.model.TipeLaporan
import com.example.temuin.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

val DAFTAR_POS_SATPAM_UB = listOf(
    "Pos Satpam Gerbang Utama (Jl. Veteran)",
    "Pos Satpam Gerbang Mayjen Panjaitan (Betek)",
    "Pos Satpam Gerbang Suhat / Kertorajasa",
    "Pos Satpam Gedung Rektorat UB",
    "Pos Satpam Gedung Samantha Krida",
    "Pos Satpam FILKOM UB",
    "Pos Satpam Fakultas Teknik (FT) UB",
    "Pos Satpam FEB UB",
    "Pos Satpam FIA UB",
    "Pos Satpam FH UB",
    "Pos Satpam FPIK / FP UB",
    "Pos Satpam Kedokteran (FK) UB",
    "Pos Satpam Kampus II UB Dieng"
)

@Composable
fun LaporScreen(
    viewModel: LaporViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigasi langsung ke Home saat submit berhasil
    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            viewModel.resetSubmitState()
            onNavigateToHome()
        } else if (submitState is SubmitState.Error) {
            snackbarHostState.showSnackbar((submitState as SubmitState.Error).message)
            viewModel.resetSubmitState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { innerPadding ->
        LaporContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            formState = formState,
            submitState = submitState,
            onTipeChanged = viewModel::onTipeChanged,
            onJudulChanged = viewModel::onJudulChanged,
            onDeskripsiChanged = viewModel::onDeskripsiChanged,
            onWaktuChanged = viewModel::onWaktuChanged,
            onLokasiPenemuanChanged = viewModel::onLokasiPenemuanChanged,
            onPertanyaanChanged = viewModel::onPertanyaanChanged,
            onMetodeChanged = viewModel::onMetodeChanged,
            onLokasiPenitipanChanged = viewModel::onLokasiPenitipanChanged,
            onLokasiKehilanganChanged = viewModel::onLokasiKehilanganChanged,
            onAddFotos = viewModel::onAddFotos,
            onRemoveFoto = viewModel::onRemoveFoto,
            onSubmit = viewModel::submitLaporan
        )
    }
}

@Composable
fun LaporContent(
    modifier: Modifier = Modifier,
    formState: LaporanFormData,
    submitState: SubmitState,
    onTipeChanged: (TipeLaporan) -> Unit,
    onJudulChanged: (String) -> Unit,
    onDeskripsiChanged: (String) -> Unit,
    onWaktuChanged: (String) -> Unit,
    onLokasiPenemuanChanged: (String) -> Unit,
    onPertanyaanChanged: (String) -> Unit,
    onMetodeChanged: (MetodePenyerahan) -> Unit,
    onLokasiPenitipanChanged: (String) -> Unit,
    onLokasiKehilanganChanged: (String) -> Unit,
    onAddFotos: (List<String>) -> Unit,
    onRemoveFoto: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isFormValid = remember(formState) {
        val hasJudul = formState.judulBarang.isNotBlank()
        if (formState.tipe == TipeLaporan.DITEMUKAN) {
            hasJudul && formState.lokasiPenemuan.isNotBlank()
        } else {
            hasJudul && formState.lokasiKehilangan.isNotBlank()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Campus Banner
        InformasiBanner()

        // 2. Segmented Choice: Tipe Pelaporan
        TipePelaporanSelector(
            selectedTipe = formState.tipe,
            onTipeSelected = onTipeChanged
        )

        // 3. Upload Foto Section (Mendukung Multiple Image)
        FotoUploadSection(
            isHilang = formState.tipe == TipeLaporan.HILANG,
            fotoUris = formState.fotoUris,
            onAddFotos = onAddFotos,
            onRemoveFoto = onRemoveFoto
        )

        // 4. Nama / Judul Barang
        LaporFormField(
            label = "Nama / Judul Barang",
            isRequired = true,
            value = formState.judulBarang,
            onValueChange = onJudulChanged,
            placeholder = "Contoh: KTM Mahasiswa UB Fakultas Ilmu Komputer",
            helperText = "Sebutkan nama barang secara ringkas dan mudah dicari."
        )

        // 5. Deskripsi Barang
        LaporFormField(
            label = "Deskripsi Barang",
            isRequired = false,
            value = formState.deskripsi,
            onValueChange = onDeskripsiChanged,
            placeholder = "Ciri-ciri khusus, warna, merk, stiker, atau kondisi barang...",
            minLines = 3,
            singleLine = false
        )

        // 6. Conditional: Waktu Penemuan (hanya DITEMUKAN)
        AnimatedVisibility(
            visible = formState.tipe == TipeLaporan.DITEMUKAN,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            WaktuPenemuanField(
                value = formState.waktuPenemuan,
                onWaktuSelected = onWaktuChanged
            )
        }

        // 7. Lokasi Kampus (Logo/Icon dihapus sesuai permintaan)
        if (formState.tipe == TipeLaporan.DITEMUKAN) {
            LaporFormField(
                label = "Lokasi Penemuan di Kampus",
                isRequired = true,
                value = formState.lokasiPenemuan,
                onValueChange = onLokasiPenemuanChanged,
                placeholder = "Contoh: Kantin Gedung B FILKOM UB"
            )
        } else {
            LaporFormField(
                label = "Lokasi Perkiraan Kehilangan",
                isRequired = true,
                value = formState.lokasiKehilangan,
                onValueChange = onLokasiKehilanganChanged,
                placeholder = "Contoh: Gazebo FIB atau Perpustakaan Pusat"
            )
        }

        // 8. Conditional: Pertanyaan Verifikasi Kepemilikan (hanya DITEMUKAN)
        AnimatedVisibility(
            visible = formState.tipe == TipeLaporan.DITEMUKAN,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LaporFormField(
                label = "Pertanyaan Verifikasi Kepemilikan",
                isRequired = false,
                value = formState.pertanyaanVerifikasi,
                onValueChange = onPertanyaanChanged,
                placeholder = "Contoh: Sebutkan isi gantungan kunci atau wallpaper layar kunci",
                helperText = "Pertanyaan ini membantu memastikan barang diserahkan ke pemilik aslinya."
            )
        }

        // 9. Conditional: Metode Penyerahan Barang + Dropdown Pos Satpam (hanya DITEMUKAN)
        AnimatedVisibility(
            visible = formState.tipe == TipeLaporan.DITEMUKAN,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            MetodePenyerahanSection(
                selectedMetode = formState.metodePenyerahan,
                selectedLokasiPenitipan = formState.lokasiPenitipan,
                onMetodeSelected = onMetodeChanged,
                onLokasiPenitipanSelected = onLokasiPenitipanChanged
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 10. CTA & Disclaimer Section
        LaporCTASection(
            isDitemukan = formState.tipe == TipeLaporan.DITEMUKAN,
            isEnabled = isFormValid && submitState !is SubmitState.Loading,
            isLoading = submitState is SubmitState.Loading,
            onSubmit = onSubmit
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun InformasiBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = OrangeLight,
        border = BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✋",
                    fontSize = 20.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bantu Sesama Arek Brawijaya",
                    style = Typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = OrangePrimaryVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Laporkan barang temuan atau kehilanganmu dengan detail agar cepat teridentifikasi oleh pemiliknya.",
                    style = Typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = TextPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TipePelaporanSelector(
    selectedTipe: TipeLaporan,
    onTipeSelected: (TipeLaporan) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "TIPE PELAPORAN",
            style = Typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            ),
            color = TextSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFECE8), shape = RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Option 1: Barang Hilang
            val isHilang = selectedTipe == TipeLaporan.HILANG
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTipeSelected(TipeLaporan.HILANG) },
                shape = RoundedCornerShape(12.dp),
                color = if (isHilang) Color.White else Color.Transparent,
                border = if (isHilang) BorderStroke(1.5.dp, OrangePrimary) else null,
                shadowElevation = if (isHilang) 2.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isHilang) OrangePrimary else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Barang Hilang",
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = if (isHilang) OrangePrimary else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Saya mencari barang",
                        style = Typography.bodyMedium.copy(fontSize = 11.sp),
                        color = if (isHilang) TextPrimary else TextHint
                    )
                }
            }

            // Option 2: Barang Ditemukan
            val isDitemukan = selectedTipe == TipeLaporan.DITEMUKAN
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTipeSelected(TipeLaporan.DITEMUKAN) },
                shape = RoundedCornerShape(12.dp),
                color = if (isDitemukan) Color.White else Color.Transparent,
                border = if (isDitemukan) BorderStroke(1.5.dp, OrangePrimary) else null,
                shadowElevation = if (isDitemukan) 2.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDitemukan) OrangePrimary else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Barang Ditemukan",
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = if (isDitemukan) OrangePrimary else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Saya menemukan barang",
                        style = Typography.bodyMedium.copy(fontSize = 11.sp),
                        color = if (isDitemukan) TextPrimary else TextHint
                    )
                }
            }
        }
    }
}

@Composable
fun FotoUploadSection(
    isHilang: Boolean,
    fotoUris: List<String>,
    onAddFotos: (List<String>) -> Unit,
    onRemoveFoto: (String) -> Unit
) {
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onAddFotos(uris.map { it.toString() })
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isHilang) "Foto Barang Hilang" else "Foto Barang Temuan",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    color = TextPrimary
                )
                if (fotoUris.isNotEmpty()) {
                    Text(
                        text = "(${fotoUris.size}/5)",
                        style = Typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    )
                }
            }
            Text(
                text = "Maks. 5 foto (5MB)",
                style = Typography.bodyMedium.copy(fontSize = 11.sp),
                color = TextHint
            )
        }

        if (fotoUris.isNotEmpty()) {
            val horizontalScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(14.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(12.dp)
                    .horizontalScroll(horizontalScroll),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                fotoUris.forEach { uri ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Foto Barang",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = { onRemoveFoto(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus foto",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                if (fotoUris.size < 5) {
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { multiplePhotoPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(10.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Foto",
                                tint = OrangePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tambah",
                                style = Typography.labelMedium.copy(fontSize = 11.sp, color = TextSecondary)
                            )
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { multiplePhotoPickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(OrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Foto",
                            tint = OrangePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unggah Foto Barang",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mendukung multi-foto (hingga 5 foto dari galeri)",
                        style = Typography.bodyMedium.copy(fontSize = 11.sp),
                        color = TextHint
                    )
                }
            }
        }
    }
}

@Composable
fun LaporFormField(
    label: String,
    isRequired: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    helperText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = TextPrimary
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Red
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    style = Typography.bodyMedium.copy(color = TextHint, fontSize = 13.sp)
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = OrangePrimary
            ),
            textStyle = Typography.bodyLarge.copy(fontSize = 14.sp, color = TextPrimary)
        )

        if (!helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                style = Typography.bodyMedium.copy(fontSize = 11.sp),
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun WaktuPenemuanField(
    value: String,
    onWaktuSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val openDateTimePicker = {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val selectedCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                        }
                        val sdf = SimpleDateFormat("EEEE, d MMM yyyy - HH:mm 'WIB'", Locale.forLanguageTag("id-ID"))
                        onWaktuSelected(sdf.format(selectedCal.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Waktu Penemuan",
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = TextPrimary
            )
            Text(
                text = " *",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Red
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { openDateTimePicker() },
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (value.isNotBlank()) value else "Pilih tanggal dan jam penemuan...",
                    style = Typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        color = if (value.isNotBlank()) TextPrimary else TextHint
                    )
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Pilih Waktu",
                    tint = OrangePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetodePenyerahanSection(
    selectedMetode: MetodePenyerahan,
    selectedLokasiPenitipan: String,
    onMetodeSelected: (MetodePenyerahan) -> Unit,
    onLokasiPenitipanSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Metode Penyerahan Barang",
                style = Typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = TextPrimary
            )

            // Option 1: Pos Satpam Kampus (dengan Dropdown pilihan pos)
            val isSatpam = selectedMetode == MetodePenyerahan.POS_SATPAM
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSatpam) OrangeLight else CardBackground)
                    .border(
                        width = if (isSatpam) 1.dp else 0.5.dp,
                        color = if (isSatpam) OrangePrimary else BorderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onMetodeSelected(MetodePenyerahan.POS_SATPAM) }
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RadioButton(
                        selected = isSatpam,
                        onClick = { onMetodeSelected(MetodePenyerahan.POS_SATPAM) },
                        colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary),
                        modifier = Modifier.size(20.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Titip di Pos Satpam Kampus",
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Barang disimpan aman di loker satpam resmi sampai pemilik mengambilnya.",
                            style = Typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = TextSecondary
                        )
                    }
                }

                // Dropdown Pilihan Pos Satpam UB saat opsi satpam dipilih
                if (isSatpam) {
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 30.dp, top = 2.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedLokasiPenitipan.ifBlank { DAFTAR_POS_SATPAM_UB.first() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Lokasi Pos Satpam", fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            textStyle = Typography.bodyMedium.copy(fontSize = 12.sp, color = TextPrimary)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DAFTAR_POS_SATPAM_UB.forEach { lokasi ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = lokasi,
                                            style = Typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = if (lokasi == selectedLokasiPenitipan) FontWeight.Bold else FontWeight.Normal,
                                                color = if (lokasi == selectedLokasiPenitipan) OrangePrimary else TextPrimary
                                            )
                                        )
                                    },
                                    onClick = {
                                        onLokasiPenitipanSelected(lokasi)
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Option 2: Langsung ke Pemilik
            val isLangsung = selectedMetode == MetodePenyerahan.LANGSUNG_PEMILIK
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLangsung) OrangeLight else CardBackground)
                    .border(
                        width = if (isLangsung) 1.dp else 0.5.dp,
                        color = if (isLangsung) OrangePrimary else BorderColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onMetodeSelected(MetodePenyerahan.LANGSUNG_PEMILIK) }
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioButton(
                    selected = isLangsung,
                    onClick = { onMetodeSelected(MetodePenyerahan.LANGSUNG_PEMILIK) },
                    colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary),
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Langsung Kembalikan ke Pemilik",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Serahkan langsung setelah bukti kepemilikan terverifikasi cocok.",
                        style = Typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun LaporCTASection(
    isDitemukan: Boolean,
    isEnabled: Boolean,
    isLoading: Boolean,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onSubmit,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                disabledContainerColor = OrangePrimary.copy(alpha = 0.4f),
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.8f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isDitemukan) "Kirim Laporan Temuan" else "Kirim Laporan Kehilangan",
                        style = Typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = TextHint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Datamu aman & hanya digunakan untuk verifikasi di kampus UB.",
                style = Typography.bodyMedium.copy(fontSize = 11.sp),
                color = TextHint,
                textAlign = TextAlign.Center
            )
        }
    }
}
