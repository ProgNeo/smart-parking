package com.valerine.ar.core.database.models

data class ParkingPlace(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val isEmployed: Boolean = false,
    var isBooked: Boolean = false,
    val parkingId: Int
) {
    companion object {
        fun mock() = arrayListOf(
            ParkingPlace(
                id = 0,
                latitude = 52.250710,
                longitude = 104.259506,
                isEmployed = false,
                isBooked = false,
                parkingId = 0
            ),
            ParkingPlace(
                id = 1,
                latitude = 52.250689,
                longitude = 104.259512,
                isEmployed = false,
                isBooked = false,
                parkingId = 0
            ),
            ParkingPlace(
                id = 2,
                latitude = 52.250674,
                longitude = 104.259449,
                isEmployed = false,
                isBooked = false,
                parkingId = 0
            ),
            ParkingPlace(
                id = 3,
                latitude = 52.250650,
                longitude = 104.259463,
                isEmployed = false,
                isBooked = false,
                parkingId = 0
            ),
            ParkingPlace(
                id = 4,
                latitude = 52.250631,
                longitude = 104.259480,
                isEmployed = false,
                isBooked = false,
                parkingId = 0
            ),
            ParkingPlace(
                id = 5,
                latitude = 52.250641,
                longitude = 104.259538,
                isEmployed = true,
                isBooked = false,
                parkingId = 0
            )
        )
    }
}