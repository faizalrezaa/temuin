package com.example.temuin.data.model

enum class PickupPointIcon {
    SECURITY,
    LIBRARY,
    FACULTY,
    STUDENT_CENTER
}

data class PickupPoint(
    val id: String,
    val name: String,
    val shortName: String,
    val locationDetail: String,
    val iconType: PickupPointIcon,
    val latitude: Double,
    val longitude: Double
)
