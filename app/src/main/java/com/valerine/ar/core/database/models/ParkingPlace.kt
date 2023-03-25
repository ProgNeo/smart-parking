package com.valerine.ar.core.database.models

data class ParkingPlace(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val employed: Boolean = false,
    val booked: Boolean = false,
    val parkingId: Int
)
