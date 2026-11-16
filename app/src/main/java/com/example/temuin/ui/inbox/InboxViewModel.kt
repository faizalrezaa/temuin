package com.example.temuin.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.model.InboxFilter
import com.example.temuin.data.model.InboxItem
import com.example.temuin.data.repository.InboxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InboxViewModel(private val repository: InboxRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(InboxFilter.SEMUA)
    val selectedFilter: StateFlow<InboxFilter> = _selectedFilter.asStateFlow()

    private val _loadingIds = MutableStateFlow<Set<String>>(emptySet())
    val loadingIds: StateFlow<Set<String>> = _loadingIds.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult.asStateFlow()

    private val allItems = repository.getInboxItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredItems: StateFlow<List<InboxItem>> = combine(allItems, _selectedFilter) { items, filter ->
        when (filter) {
            InboxFilter.SEMUA -> items
            InboxFilter.PERMINTAAN_KLAIM -> items.filterIsInstance<InboxItem.PermintaanKlaim>()
            InboxFilter.STATUS_LAPORAN -> items.filterIsInstance<InboxItem.StatusLaporan>()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counts: StateFlow<Map<InboxFilter, Int>> = allItems.combine(_selectedFilter) { items, _ ->
        mapOf(
            InboxFilter.SEMUA to items.size,
            InboxFilter.PERMINTAAN_KLAIM to items.filterIsInstance<InboxItem.PermintaanKlaim>().size,
            InboxFilter.STATUS_LAPORAN to items.filterIsInstance<InboxItem.StatusLaporan>().size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun selectFilter(filter: InboxFilter) {
        _selectedFilter.value = filter
    }

    fun setujuiKlaim(id: String) {
        viewModelScope.launch {
            _loadingIds.value = _loadingIds.value + id
            repository.setujuiKlaim(id)
                .onSuccess { _actionResult.value = "Klaim berhasil disetujui." }
                .onFailure { _actionResult.value = "Gagal menyetujui klaim. Coba lagi." }
            _loadingIds.value = _loadingIds.value - id
        }
    }

    fun tolakKlaim(id: String) {
        viewModelScope.launch {
            _loadingIds.value = _loadingIds.value + id
            repository.tolakKlaim(id)
                .onSuccess { _actionResult.value = "Klaim berhasil ditolak." }
                .onFailure { _actionResult.value = "Gagal menolak klaim. Coba lagi." }
            _loadingIds.value = _loadingIds.value - id
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
