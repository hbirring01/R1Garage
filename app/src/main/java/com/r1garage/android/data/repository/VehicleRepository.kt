package com.r1garage.android.data.repository

import com.r1garage.android.data.detector.SessionDetector
import com.r1garage.android.data.local.VehicleSnapshotDao
import com.r1garage.android.data.local.VehicleSnapshotEntity
import com.r1garage.android.data.rivian.AuthDtosJson
import com.r1garage.android.data.rivian.GraphQlRequest
import com.r1garage.android.data.rivian.RivianApi
import com.r1garage.android.data.rivian.RivianQueries
import com.r1garage.android.data.rivian.VehicleStateData
import com.r1garage.android.data.rivian.VehicleStateDto
import com.r1garage.android.domain.model.VehicleSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val api: RivianApi,
    private val snapshotDao: VehicleSnapshotDao,
    private val sessionDetector: SessionDetector,
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
                variables = buildJsonObject {
                    put("vehicleID", JsonPrimitive(vehicleId))
                }
            )
        )
        response.errors?.firstOrNull()?.let {
            throw IllegalStateException(it.message)
        }
        val data = response.data ?: throw IllegalStateException("no data in response")
        val state = AuthDtosJson.decodeFromJsonElement<VehicleStateData>(data).vehicleState
            ?: throw IllegalStateException("no vehicleState in response")
        snapshotDao.insert(state.toEntity(System.currentTimeMillis()))

        // Run trip / charge session detection against the snapshot we just
        // inserted. Worst case the detector no-ops; it never wakes the car.
        sessionDetector.onNewSnapshot(snapshotDao.recent(SESSION_LOOKBACK))

        // Keep ~30 days of history. The poller runs every 15 min by default
        // so this caps the table around 3k rows.
        snapshotDao.pruneOlderThan(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
    }

    companion object {
        /**
         * How many recent snapshots to hand to the [SessionDetector]. Even
         * with a 4 h low-power throttle, 96 snapshots covers >24 h — plenty
         * for finding session boundaries.
         */
        private const val SESSION_LOOKBACK = 96
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
    powerState = powerState?.value,
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
