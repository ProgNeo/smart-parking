package com.valerine.ar.core.database.models

data class ParkingPlace(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val isEmployed: Boolean = false,
    val isBooked: Boolean = false,
    val parkingId: Int
)
