package com.r1garage.android.domain.model

/**
 * Domain-layer projection of a Rivian vehicle state poll. UI binds to this
 * shape; the Rivian DTO and the Room entity both convert into it so a future
 * API shape change is isolated to the mapper.
 */
data class VehicleSnapshot(
    val fetchedAt: Long,
    val vehicleName: String?,
    val socPct: Int?,
    val rangeMi: Int?,
    val odometerMi: Int?,
    val gear: String?,
    val locked: Boolean?,
    val plugState: String?,
    val chargingKw: Double?,
    val lat: Double?,
    val lon: Double?,
    val twelveVolt: Double?,
)
