package com.example.temuin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.model.Item
import com.example.temuin.data.repository.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Status laporan untuk filter (nilai = parameter `status` di GET /reports). */
enum class StatusFilter(val apiValue: String, val label: String) {
    AKTIF("ACTIVE", "Aktif"),
    SELESAI("COMPLETED", "Selesai"),
    SEMUA("ALL", "Semua"),
}

class HomeViewModel(
    private val repository: ItemRepository,
    initialLocationFilter: String? = null
) : ViewModel() {

    private val _locationFilter = MutableStateFlow(initialLocationFilter ?: "")
    val locationFilter: StateFlow<String> = _locationFilter.asStateFlow()

    /** Kode kategori dari API (null = semua kategori). */
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.AKTIF)
    val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

    /** Pilihan kategori dari GET /categories: (kode, label). */
    private val _categories = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val categories: StateFlow<List<Pair<String, String>>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<Item>> =
        combine(refreshTrigger, _categoryFilter, _statusFilter) { _, category, status -> category to status }
            .flatMapLatest { (category, status) ->
                _isLoading.value = true
                repository.getItems(category, status.apiValue)
            }
            .onEach { _isLoading.value = false }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch { _categories.value = repository.getCategories() }
    }

    /** Ambil ulang feed dari server (dipanggil setiap Home tampil). */
    fun refresh() {
        refreshTrigger.value++
    }

    fun applyFilters(category: String?, status: StatusFilter) {
        _categoryFilter.value = category
        _statusFilter.value = status
    }

    fun resetFilters() = applyFilters(null, StatusFilter.AKTIF)

    fun setLocationFilter(filter: String) {
        _locationFilter.value = filter
    }

    fun clearLocationFilter() {
        _locationFilter.value = ""
    }
}
