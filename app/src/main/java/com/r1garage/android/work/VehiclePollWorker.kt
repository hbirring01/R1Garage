package com.r1garage.android.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.r1garage.android.data.local.AlertEvent
import com.r1garage.android.data.local.AlertEventDao
import com.r1garage.android.data.local.VehicleSnapshotDao
import com.r1garage.android.data.local.VehicleSnapshotEntity
import com.r1garage.android.data.preferences.PreferencesRepository
import com.r1garage.android.data.repository.VehicleRepository
import com.r1garage.android.data.rivian.RivianTokenStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Polls the Rivian API for the latest vehicle state.
 *
 * **12 V battery safety contract — DO NOT VIOLATE:**
 *  - This worker calls ONLY [VehicleRepository.refresh], which issues the
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
 * **Adaptive cadence** (decisions based on the last snapshot's `power_state`,
 * `gear`, and charger fields — see [Activity]):
 *  - DRIVING (gear != park, powerState `ready`/`go`/`drive`): poll every tick
 *    (~15 min, the WorkManager floor) to keep the live dashboard fresh.
 *  - CHARGING (chargerState contains `charging`): poll every tick so the
 *    charge-session detector sees both endpoints of the curve.
 *  - QUIESCENT (parked & not charging; powerState `sleep`/`standby`/null):
 *    skip the network call if the last snapshot is younger than the
 *    quiescent throttle. Window depends on user preference:
 *      - Low-power mode OFF (default): [QUIESCENT_THROTTLE_NORMAL_MS] (~55 min)
 *      - Low-power mode ON: [QUIESCENT_THROTTLE_LOW_POWER_MS] (~4 h)
 *
 * Either way this only affects phone-side battery / data use — the vehicle
 * is never contacted.
 */
@HiltWorker
class VehiclePollWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: VehicleRepository,
    private val tokenStore: RivianTokenStore,
    private val alertDao: AlertEventDao,
    private val snapshotDao: VehicleSnapshotDao,
    private val preferences: PreferencesRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val vehicleId = inputData.getString(KEY_VEHICLE_ID) ?: VEHICLE_ID_PLACEHOLDER
        if (!tokenStore.isSignedIn || vehicleId == VEHICLE_ID_PLACEHOLDER) {
            // Not signed in yet — treat as soft success so WorkManager doesn't
            // back off. The UI will show "Not signed in" until the user logs in.
            return Result.success()
        }

        val last = snapshotDao.latest()
        if (last != null && shouldSkip(last, preferences.lowPowerModeOnce())) {
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

    private fun shouldSkip(last: VehicleSnapshotEntity, lowPowerMode: Boolean): Boolean {
        val age = System.currentTimeMillis() - last.fetchedAt
        return when (last.classify()) {
            Activity.Driving, Activity.Charging -> false
            Activity.Quiescent -> {
                val window = if (lowPowerMode) {
                    QUIESCENT_THROTTLE_LOW_POWER_MS
                } else {
                    QUIESCENT_THROTTLE_NORMAL_MS
                }
                age < window
            }
        }
    }

    enum class Activity { Driving, Charging, Quiescent }

    private fun VehicleSnapshotEntity.classify(): Activity {
        val driving = gear?.equals("park", ignoreCase = true) == false ||
            powerState?.lowercase() in setOf("ready", "go", "drive")
        if (driving) return Activity.Driving
        val charging = plugState?.contains("charging", ignoreCase = true) == true
        if (charging) return Activity.Charging
        return Activity.Quiescent
    }

    companion object {
        const val KEY_VEHICLE_ID = "vehicle_id"
        const val VEHICLE_ID_PLACEHOLDER = "<unset>"

        /** Default throttle while parked + unplugged. */
        private val QUIESCENT_THROTTLE_NORMAL_MS = TimeUnit.MINUTES.toMillis(55)

        /** Low-power-mode throttle. ~4 h between idle polls. */
        private val QUIESCENT_THROTTLE_LOW_POWER_MS = TimeUnit.HOURS.toMillis(4)
    }
}
