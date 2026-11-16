package com.example.temuin.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.model.Item
import com.example.temuin.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ItemDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ItemRepository
) : ViewModel() {
    val itemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _item = MutableStateFlow<Item?>(null)
    val item: StateFlow<Item?> = _item.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _contactLink = MutableStateFlow<String?>(null)
    val contactLink: StateFlow<String?> = _contactLink.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    private val _claimError = MutableStateFlow<String?>(null)
    val claimError: StateFlow<String?> = _claimError.asStateFlow()
    fun clearClaimError() { _claimError.value = null }

    init { load() }

    private fun load() {
        viewModelScope.launch {
            repository.getItemById(itemId).collect { loaded ->
                _item.value = loaded
                if (loaded == null) _message.value = "Gagal memuat detail barang"
            }
        }
    }

    fun submitClaim(secretDescription: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _submitting.value = true
            repository.submitClaim(itemId, secretDescription)
                .onSuccess {
                    _message.value = "Klaim terkirim. Pantau statusnya di Inbox."
                    onDone()
                    load() // status tombol ikut berubah
                }
                .onFailure { _claimError.value = it.message ?: "Gagal mengirim klaim" }
            _submitting.value = false
        }
    }

    fun contactReporter() {
        viewModelScope.launch {
            repository.getContactLink(itemId)
                .onSuccess { _contactLink.value = it }
                .onFailure { _message.value = it.message }
        }
    }

    fun clearMessage() { _message.value = null }
    fun clearContactLink() { _contactLink.value = null }
}
