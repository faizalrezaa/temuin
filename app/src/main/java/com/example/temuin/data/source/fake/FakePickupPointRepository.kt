package com.example.temuin.data.source.fake

import com.example.temuin.data.model.PickupPoint
import com.example.temuin.data.model.PickupPointIcon
import com.example.temuin.data.repository.PickupPointRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePickupPointRepository : PickupPointRepository {
    private val points = listOf(
        PickupPoint(
            id = "perpus_pusat",
            name = "Pos Satpam Perpustakaan Pusat",
            shortName = "Perpus Pusat",
            locationDetail = "Lantai 1, Dekat Pintu Masuk Sayap Barat",
            iconType = PickupPointIcon.LIBRARY,
            latitude = -7.952900,
            longitude = 112.613300
        ),
        PickupPoint(
            id = "rektorat",
            name = "Pos Satpam Utama Rektorat",
            shortName = "Rektorat UB",
            locationDetail = "Lobi Utama Gedung Rektorat UB",
            iconType = PickupPointIcon.SECURITY,
            latitude = -7.952485,
            longitude = 112.614480
        ),
        PickupPoint(
            id = "pos_ft",
            name = "Pos Keamanan Fakultas Teknik",
            shortName = "Teknik UB",
            locationDetail = "Pintu Masuk Utama Gedung Dekanat FT",
            iconType = PickupPointIcon.FACULTY,
            latitude = -7.955400,
            longitude = 112.614900
        ),
        PickupPoint(
            id = "student_center_feb",
            name = "Student Center & BEM FEB",
            shortName = "FEB UB",
            locationDetail = "Gedung Student Center Lt. 1, FEB UB",
            iconType = PickupPointIcon.STUDENT_CENTER,
            latitude = -7.954100,
            longitude = 112.612700
        )
    )

    override fun getPickupPoints(): Flow<List<PickupPoint>> = flowOf(points)
}
