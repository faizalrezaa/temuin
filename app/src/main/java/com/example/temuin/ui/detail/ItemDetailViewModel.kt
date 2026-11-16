package com.example.temuin.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.model.Item
import com.example.temuin.data.repository.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ItemDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: ItemRepository
) : ViewModel() {
    val itemId: String = checkNotNull(savedStateHandle["itemId"])

    val item: StateFlow<Item?> = repository.getItemById(itemId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
