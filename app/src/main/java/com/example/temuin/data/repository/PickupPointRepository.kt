package com.example.temuin.data.repository

import com.example.temuin.data.model.PickupPoint
import kotlinx.coroutines.flow.Flow

interface PickupPointRepository {
    fun getPickupPoints(): Flow<List<PickupPoint>>
}
