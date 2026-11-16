package com.example.temuin.data.source.remote

import android.util.Log
import com.example.temuin.data.model.PickupPoint
import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.ApiMappers
import com.example.temuin.data.remote.TemuinApi
import com.example.temuin.data.repository.PickupPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemotePickupPointRepository(private val api: TemuinApi = ApiClient.api) : PickupPointRepository {
    override fun getPickupPoints(): Flow<List<PickupPoint>> = flow {
        val points = runCatching {
            api.pickupPoints().data.orEmpty()
                .filter { it.latitude != null && it.longitude != null }
                .map(ApiMappers::toPickupPoint)
        }.onFailure { Log.w("RemotePickupRepo", ApiClient.errorMessage(it)) }
            .getOrDefault(emptyList())
        emit(points)
    }
}
