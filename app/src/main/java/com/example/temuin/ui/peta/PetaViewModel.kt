package com.example.temuin.ui.peta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.model.Item
import com.example.temuin.data.model.PickupPoint
import com.example.temuin.data.repository.ItemRepository
import com.example.temuin.data.repository.PickupPointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetaUiState(
    val pickupPoints: List<PickupPoint> = emptyList(),
    val filteredPoints: List<PickupPoint> = emptyList(),
    val selectedPoint: PickupPoint? = null,
    val itemsAtSelectedPoint: List<Item> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class PetaViewModel(
    private val pickupPointRepository: PickupPointRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetaUiState())
    val uiState: StateFlow<PetaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pickupPointRepository.getPickupPoints().collect { points ->
                _uiState.update { current ->
                    current.copy(
                        pickupPoints = points,
                        filteredPoints = filterPoints(points, current.searchQuery),
                        // Default pilih Perpus Pusat bila belum ada yang terpilih (sesuai Figma state awal)
                        selectedPoint = current.selectedPoint ?: points.find { it.id == "perpus_pusat" } ?: points.firstOrNull(),
                        isLoading = false
                    )
                }
                _uiState.value.selectedPoint?.let { loadItemsForPoint(it) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredPoints = filterPoints(current.pickupPoints, query)
            )
        }
    }

    fun selectPickupPoint(point: PickupPoint?) {
        _uiState.update { it.copy(selectedPoint = point) }
        if (point != null) {
            loadItemsForPoint(point)
        } else {
            _uiState.update { it.copy(itemsAtSelectedPoint = emptyList()) }
        }
    }

    private var itemsJob: kotlinx.coroutines.Job? = null

    private fun loadItemsForPoint(point: PickupPoint) {
        itemsJob?.cancel()
        itemsJob = viewModelScope.launch {
            itemRepository.getItemsAtPickupPoint(point.id).collect { items ->
                _uiState.update { it.copy(itemsAtSelectedPoint = items) }
            }
        }
    }

    private fun filterPoints(points: List<PickupPoint>, query: String): List<PickupPoint> {
        if (query.isBlank()) return points
        return points.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.shortName.contains(query, ignoreCase = true) ||
                    it.locationDetail.contains(query, ignoreCase = true)
        }
    }
}
