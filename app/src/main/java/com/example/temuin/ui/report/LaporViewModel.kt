package com.example.temuin.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.model.LaporanFormData
import com.example.temuin.data.model.MetodePenyerahan
import com.example.temuin.data.model.TipeLaporan
import com.example.temuin.data.repository.LaporanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}

class LaporViewModel(
    private val repository: LaporanRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(LaporanFormData())
    val formState: StateFlow<LaporanFormData> = _formState.asStateFlow()

    private val _pickupPointOptions = MutableStateFlow(DAFTAR_POS_SATPAM_UB)
    val pickupPointOptions: StateFlow<List<String>> = _pickupPointOptions.asStateFlow()

    init {
        viewModelScope.launch {
            val names = repository.getPickupPointNames()
            if (names.isNotEmpty()) {
                _pickupPointOptions.value = names
                _formState.update { if (it.lokasiPenitipan in names) it else it.copy(lokasiPenitipan = names.first()) }
            }
        }
    }

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    fun onTipeChanged(tipe: TipeLaporan) = _formState.update { it.copy(tipe = tipe) }
    fun onJudulChanged(judul: String) = _formState.update { it.copy(judulBarang = judul) }
    fun onDeskripsiChanged(desc: String) = _formState.update { it.copy(deskripsi = desc) }
    fun onWaktuChanged(waktu: String) = _formState.update { it.copy(waktuPenemuan = waktu) }
    fun onLokasiPenemuanChanged(lokasi: String) = _formState.update { it.copy(lokasiPenemuan = lokasi) }
    fun onPertanyaanChanged(q: String) = _formState.update { it.copy(pertanyaanVerifikasi = q) }
    fun onMetodeChanged(metode: MetodePenyerahan) = _formState.update { it.copy(metodePenyerahan = metode) }
    fun onLokasiPenitipanChanged(lokasi: String) = _formState.update { it.copy(lokasiPenitipan = lokasi) }
    fun onLokasiKehilanganChanged(lokasi: String) = _formState.update { it.copy(lokasiKehilangan = lokasi) }

    fun onAddFotos(uris: List<String>) = _formState.update {
        val updated = (it.fotoUris + uris).distinct().take(5)
        it.copy(fotoUris = updated)
    }

    fun onRemoveFoto(uri: String) = _formState.update {
        it.copy(fotoUris = it.fotoUris.filter { item -> item != uri })
    }

    fun resetSubmitState() = _submitState.update { SubmitState.Idle }

    fun submitLaporan() {
        viewModelScope.launch {
            _submitState.update { SubmitState.Loading }
            val result = repository.submitLaporan(_formState.value)
            _submitState.update {
                if (result.isSuccess) SubmitState.Success
                else SubmitState.Error(result.exceptionOrNull()?.message ?: "Gagal mengirim laporan")
            }
        }
    }
}
