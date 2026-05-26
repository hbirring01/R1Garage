package com.r1garage.android.data.repository

import com.r1garage.android.data.detector.SessionDetector
import com.r1garage.android.data.local.VehicleSnapshotDao
import com.r1garage.android.data.local.VehicleSnapshotEntity
import com.r1garage.android.data.rivian.AuthDtosJson
import com.r1garage.android.data.rivian.GraphQlRequest
import com.r1garage.android.data.rivian.RivianApi
import com.r1garage.android.data.rivian.RivianQueries
import com.r1garage.android.data.rivian.RivianTokenStore
import com.r1garage.android.data.rivian.UserInfoData
import com.r1garage.android.data.rivian.VehicleImagesData
import com.r1garage.android.data.rivian.VehicleStateData
import com.r1garage.android.data.rivian.VehicleStateDto
import com.r1garage.android.domain.model.VehicleSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
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
    private val tokenStore: RivianTokenStore,
) {
    val latestSnapshot: Flow<VehicleSnapshot?> =
        snapshotDao.observeLatest().map { it?.toDomain() }

    /**
     * Discovers the signed-in user's enrolled vehicles and persists the
     * first one's id/name. Read-only — does not wake the car.
     *
     * Returns the discovered vehicleId, or null if the account has none.
     * Throws on transport / GraphQL errors so callers can surface them.
     */
    suspend fun enrollFirstVehicle(): String? {
        val resp = api.graphql(
            GraphQlRequest(
                operationName = "GetUserInfo",
                query = RivianQueries.GET_USER_INFO,
                variables = JsonObject(emptyMap()),
            )
        )
        resp.errors?.firstOrNull()?.let { error(it.message) }
        val data = checkNotNull(resp.data) { "no data in user info response" }
        val user = checkNotNull(
            AuthDtosJson.decodeFromJsonElement<UserInfoData>(data).currentUser
        ) { "no currentUser in response" }
        val first = user.vehicles?.firstOrNull { !it.id.isNullOrBlank() } ?: return null
        tokenStore.vehicleId = first.id
        tokenStore.vehicleName = first.name?.takeUnless { it.isBlank() }
            ?: first.vehicle?.model

        // Best-effort: enrich with a CDN image URL of the user's actual
        // vehicle. Failure here is silent — Home falls back to the generic
        // silhouette and the user can still see all telemetry.
        runCatching { fetchAndStoreVehicleImage(first.id!!) }
        return first.id
    }

    /**
     * Queries Rivian's image CDN for renderings that match the user's paint
     * + wheel config, picks an exterior front 3/4 shot, and persists the
     * URL in [tokenStore]. Caller treats failures as non-fatal.
     */
    private suspend fun fetchAndStoreVehicleImage(vehicleId: String) {
        val resp = api.graphql(
            GraphQlRequest(
                operationName = "GetVehicleImages",
                query = RivianQueries.GET_VEHICLE_IMAGES,
                variables = buildJsonObject {
                    put("extension", JsonPrimitive("webp"))
                    put("resolution", JsonPrimitive("@2x"))
                    put("versionNumber", JsonPrimitive(2))
                },
            )
        )
        if (resp.errors?.isNotEmpty() == true) return
        val data = resp.data ?: return
        val images = AuthDtosJson.decodeFromJsonElement<VehicleImagesData>(data)
            .getVehicleImages
            ?.filter { !it.url.isNullOrBlank() && it.vehicleId == vehicleId }
            .orEmpty()
        // Prefer an exterior front-3/4 hero; fall back to first exterior;
        // finally the first usable URL of any kind.
        val pick = images.firstOrNull {
            it.design?.contains("front_3qtr", ignoreCase = true) == true
        } ?: images.firstOrNull {
            it.placement?.equals("exterior", ignoreCase = true) == true
        } ?: images.firstOrNull()
        tokenStore.vehicleImageUrl = pick?.url
    }

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
        response.errors?.firstOrNull()?.let { error(it.message) }
        val data = checkNotNull(response.data) { "no data in response" }
        val state = checkNotNull(
            AuthDtosJson.decodeFromJsonElement<VehicleStateData>(data).vehicleState
        ) { "no vehicleState in response" }
        snapshotDao.insert(state.toEntity(System.currentTimeMillis(), tokenStore.vehicleName))

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

private fun VehicleStateDto.toEntity(now: Long, vehicleName: String?) = VehicleSnapshotEntity(
    fetchedAt = now,
    vehicleName = vehicleName,
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
