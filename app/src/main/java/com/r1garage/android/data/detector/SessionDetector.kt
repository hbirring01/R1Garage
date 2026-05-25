package com.r1garage.android.data.detector

import com.r1garage.android.data.local.ChargeSession
import com.r1garage.android.data.local.ChargeSessionDao
import com.r1garage.android.data.local.Trip
import com.r1garage.android.data.local.TripDao
import com.r1garage.android.data.local.VehicleSnapshotEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Detects charge and drive sessions by diffing the current snapshot against
 * the previous one stored in the DB.
 *
 *  - **Charge session end:** previous snapshot was charging, current isn't.
 *    Walk backward through contiguous charging snapshots to find the start;
 *    write a row to `charge_session`.
 *  - **Trip end:** previous snapshot had `gear != park` (or `powerState`
 *    in {ready, go, drive}), current is parked. Walk backward to find the
 *    drive start; write a row to `trip`.
 *
 * No commands are sent to the vehicle anywhere in this class — it operates
 * purely on stored snapshots.
 */
@Singleton
class SessionDetector @Inject constructor(
    private val chargeDao: ChargeSessionDao,
    private val tripDao: TripDao,
) {

    /**
     * Called by [com.r1garage.android.data.repository.VehicleRepository]
     * after a fresh snapshot has been persisted. [recent] is the last few
     * snapshots ordered newest-first (size >= 1).
     */
    suspend fun onNewSnapshot(recent: List<VehicleSnapshotEntity>) {
        if (recent.size < 2) return
        val current = recent[0]
        val previous = recent[1]

        if (previous.isCharging() && !current.isCharging()) {
            closeChargeSession(recent)
        }
        if (previous.isDriving() && !current.isDriving()) {
            closeTrip(recent)
        }
    }

    private suspend fun closeChargeSession(recent: List<VehicleSnapshotEntity>) {
        // recent[0] = first non-charging snap; recent[1..] starts with the
        // tail of the charging window. Walk forward through `recent` (which
        // is newest-first) past the charging run to find the session start.
        val chargingRun = recent.drop(1).takeWhile { it.isCharging() }
        if (chargingRun.isEmpty()) return
        val start = chargingRun.last() // oldest charging snap
        val end = chargingRun.first() // newest charging snap

        val startSoc = start.socPct ?: return
        val endSoc = end.socPct ?: return
        if (endSoc <= startSoc) return // ignore noise / discharge

        chargeDao.insert(
            ChargeSession(
                startedAt = start.fetchedAt,
                endedAt = end.fetchedAt,
                startSoc = startSoc,
                endSoc = endSoc,
                kwhAdded = (endSoc - startSoc) * KWH_PER_SOC_PCT,
                peakKw = 0.0, // populated once chargingKw is sampled
                lat = start.lat ?: end.lat,
                lon = start.lon ?: end.lon,
            )
        )
    }

    private suspend fun closeTrip(recent: List<VehicleSnapshotEntity>) {
        val drivingRun = recent.drop(1).takeWhile { it.isDriving() }
        if (drivingRun.isEmpty()) return
        val start = drivingRun.last()
        val end = drivingRun.first()

        val distanceMi = start.odometerMi?.let { s ->
            end.odometerMi?.let { e -> (e - s).toDouble().coerceAtLeast(0.0) }
        } ?: haversineMi(start.lat, start.lon, end.lat, end.lon)

        val kwhUsed = start.socPct?.let { s ->
            end.socPct?.let { e -> ((s - e).coerceAtLeast(0)) * KWH_PER_SOC_PCT }
        } ?: 0.0

        if (distanceMi < MIN_TRIP_MI && kwhUsed < MIN_TRIP_KWH) return

        tripDao.insert(
            Trip(
                startedAt = start.fetchedAt,
                endedAt = end.fetchedAt,
                distanceMi = distanceMi,
                kwhUsed = kwhUsed,
                startLat = start.lat,
                startLon = start.lon,
                endLat = end.lat,
                endLon = end.lon,
            )
        )
    }

    private fun VehicleSnapshotEntity.isCharging(): Boolean =
        plugState?.contains("charging", ignoreCase = true) == true

    private fun VehicleSnapshotEntity.isDriving(): Boolean {
        val parkedByGear = gear?.equals("park", ignoreCase = true) == true || gear.isNullOrBlank()
        if (!parkedByGear) return true
        return powerState?.lowercase() in setOf("ready", "go", "drive")
    }

    private fun haversineMi(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 0.0
        val r = 3958.7613 // earth radius in miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow2() +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow2()
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun Double.pow2() = this * this

    @Suppress("unused")
    private fun nearZero(v: Double): Boolean = abs(v) < 0.01

    companion object {
        /** Rivian R1S Large pack ~135 kWh; R1S Max ~149 kWh. 1.35 is a safe avg. */
        private const val KWH_PER_SOC_PCT: Double = 1.35

        /** Filter out spurious gear blips. */
        private const val MIN_TRIP_MI: Double = 0.25
        private const val MIN_TRIP_KWH: Double = 0.5
    }
}
