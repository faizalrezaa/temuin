package com.example.temuin.data.repository

import com.example.temuin.data.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(): Flow<List<Item>>
    fun getItemById(id: String): Flow<Item?>
}
