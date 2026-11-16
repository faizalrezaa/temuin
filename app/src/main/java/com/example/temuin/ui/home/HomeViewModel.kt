package com.example.temuin.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.model.Item
import com.example.temuin.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val repository: ItemRepository,
    initialLocationFilter: String? = null
) : ViewModel() {

    private val _locationFilter = MutableStateFlow(initialLocationFilter ?: "")
    val locationFilter: StateFlow<String> = _locationFilter.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<Item>> = refreshTrigger
        .flatMapLatest { repository.getItems() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Ambil ulang feed dari server (dipanggil setiap Home tampil). */
    fun refresh() {
        refreshTrigger.value++
    }

    fun setLocationFilter(filter: String) {
        _locationFilter.value = filter
    }

    fun clearLocationFilter() {
        _locationFilter.value = ""
    }
}

