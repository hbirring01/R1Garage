package com.r1garage.android.data.repository

import com.r1garage.android.data.local.VehicleSnapshotDao
import com.r1garage.android.data.local.VehicleSnapshotEntity
import com.r1garage.android.data.rivian.GraphQlRequest
import com.r1garage.android.data.rivian.RivianApi
import com.r1garage.android.data.rivian.RivianQueries
import com.r1garage.android.data.rivian.VehicleStateDto
import com.r1garage.android.domain.model.VehicleSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class VehicleRepository @Inject constructor(
    private val api: RivianApi,
    private val snapshotDao: VehicleSnapshotDao,
) {
    val latestSnapshot: Flow<VehicleSnapshot?> =
        snapshotDao.observeLatest().map { it?.toDomain() }

    /**
     * Fetches a fresh snapshot and persists it. Throws when not authenticated
     * or when the network call fails — callers (the poller) decide whether
     * to surface that as an alert.
     */
    suspend fun refresh(vehicleId: String) {
        val response = api.graphql(
            GraphQlRequest(
                operationName = "GetVehicleState",
                query = RivianQueries.GET_VEHICLE_STATE,
                variables = mapOf("vehicleID" to vehicleId),
            )
        )
        val state = response.data?.vehicleState
            ?: throw IllegalStateException(
                response.errors?.joinToString { it.message } ?: "no vehicleState in response"
            )
        snapshotDao.insert(state.toEntity(System.currentTimeMillis()))
        // Keep ~30 days of history. The poller runs every 15 min by default
        // so this caps the table around 3k rows.
        snapshotDao.pruneOlderThan(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    }
}

private fun VehicleStateDto.toEntity(now: Long) = VehicleSnapshotEntity(
    fetchedAt = now,
    vehicleName = null,
    socPct = batteryLevel?.value?.toDoubleOrNull()?.toInt(),
    rangeMi = distanceToEmpty?.value?.toDoubleOrNull()?.let { kmToMi(it) },
    odometerMi = vehicleMileage?.value?.toDoubleOrNull()?.let { kmToMi(it) },
    gear = gearStatus?.value,
    locked = doorFrontLeftLocked?.value?.equals("locked", ignoreCase = true),
    plugState = chargerState?.value,
    chargingKw = null,
    lat = gnssLocation?.latitude,
    lon = gnssLocation?.longitude,
    twelveVolt = twelveVoltBatteryHealth?.value?.toDoubleOrNull(),
)

private fun VehicleSnapshotEntity.toDomain() = VehicleSnapshot(
    fetchedAt = fetchedAt,
    vehicleName = vehicleName,
    socPct = socPct,
    rangeMi = rangeMi,
    odometerMi = odometerMi,
    gear = gear,
    locked = locked,
    plugState = plugState,
    chargingKw = chargingKw,
    lat = lat,
    lon = lon,
    twelveVolt = twelveVolt,
)

private fun kmToMi(km: Double): Int = (km * 0.621371).toInt()
