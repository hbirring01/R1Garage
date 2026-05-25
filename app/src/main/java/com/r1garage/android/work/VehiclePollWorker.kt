package com.r1garage.android.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.r1garage.android.data.local.AlertEvent
import com.r1garage.android.data.local.AlertEventDao
import com.r1garage.android.data.local.VehicleSnapshotDao
import com.r1garage.android.data.repository.VehicleRepository
import com.r1garage.android.data.rivian.RivianTokenStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Polls the Rivian API for the latest vehicle state.
 *
 * **12 V battery safety contract — DO NOT VIOLATE:**
 *  - This worker calls ONLY `VehicleRepository.refresh`, which issues the
 *    read-only `vehicleState` GraphQL query against the consumer endpoint.
 *  - `vehicleState` returns cached telemetry held by Rivian's cloud; the
 *    cloud is populated by pushes FROM the vehicle while it is already
 *    awake. Reading it does not initiate any communication with the car
 *    and therefore cannot wake it.
 *  - Operations that DO wake the car (and must never be added here):
 *    `subscribeVehicle` (live websocket), any `liveVehicleState` /
 *    `getLiveSessionData` endpoint, and any command mutation
 *    (`sendVehicleCommand`, lock/unlock, precondition, etc.).
 *
 * Throttle: when the last snapshot says the car is parked AND not actively
 * charging AND was fetched < [QUIESCENT_THROTTLE_MS] ago, skip the network
 * call this tick. WorkManager fires every ~15 min minimum; this collapses
 * us to roughly one network read per hour while the car is idle.
 */
@HiltWorker
class VehiclePollWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: VehicleRepository,
    private val tokenStore: RivianTokenStore,
    private val alertDao: AlertEventDao,
    private val snapshotDao: VehicleSnapshotDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val vehicleId = inputData.getString(KEY_VEHICLE_ID) ?: VEHICLE_ID_PLACEHOLDER
        if (!tokenStore.isSignedIn || vehicleId == VEHICLE_ID_PLACEHOLDER) {
            // Not signed in yet — treat as soft success so WorkManager doesn't
            // back off. The UI will show "Not signed in" until the user logs in.
            return Result.success()
        }

        // Adaptive throttle: when the car is quiescent (parked & not actively
        // charging) and we already have a recent snapshot, skip the network
        // call. Saves phone battery / data; does not affect the vehicle.
        val last = snapshotDao.latest()
        if (last != null && last.isQuiescent() &&
            System.currentTimeMillis() - last.fetchedAt < QUIESCENT_THROTTLE_MS
        ) {
            return Result.success()
        }

        return try {
            repository.refresh(vehicleId)
            Result.success()
        } catch (t: Throwable) {
            alertDao.insert(
                AlertEvent(
                    triggeredAt = System.currentTimeMillis(),
                    kind = "Poll error",
                    message = t.message ?: t::class.java.simpleName,
                )
            )
            Result.retry()
        }
    }

    private fun com.r1garage.android.data.local.VehicleSnapshotEntity.isQuiescent(): Boolean {
        val parked = gear == null || gear.equals("park", ignoreCase = true)
        val notCharging = plugState == null ||
            !plugState.equals("charging_active", ignoreCase = true) &&
            !plugState.contains("charging", ignoreCase = true)
        return parked && notCharging
    }

    companion object {
        const val KEY_VEHICLE_ID = "vehicle_id"
        const val VEHICLE_ID_PLACEHOLDER = "<unset>"

        /** Skip network polls when quiescent if last fetch was within this window. */
        private val QUIESCENT_THROTTLE_MS = TimeUnit.MINUTES.toMillis(55)
    }
}
