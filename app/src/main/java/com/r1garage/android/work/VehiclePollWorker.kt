package com.r1garage.android.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.r1garage.android.data.local.AlertEvent
import com.r1garage.android.data.local.AlertEventDao
import com.r1garage.android.data.repository.VehicleRepository
import com.r1garage.android.data.rivian.RivianTokenStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Polls the Rivian API for the latest vehicle state. Scheduled via
 * [VehiclePollScheduler] as a periodic WorkManager job (every 15 min
 * minimum on Android). Trip/charge detection and alert evaluation will
 * be plugged into this worker as we build out the detector logic.
 */
@HiltWorker
class VehiclePollWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: VehicleRepository,
    private val tokenStore: RivianTokenStore,
    private val alertDao: AlertEventDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val token = tokenStore.accessToken
        val vehicleId = inputData.getString(KEY_VEHICLE_ID) ?: VEHICLE_ID_PLACEHOLDER
        if (token.isNullOrBlank() || vehicleId == VEHICLE_ID_PLACEHOLDER) {
            // Not signed in yet — treat as soft success so WorkManager doesn't
            // back off. The UI will show "Not signed in" until the user logs in.
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

    companion object {
        const val KEY_VEHICLE_ID = "vehicle_id"
        const val VEHICLE_ID_PLACEHOLDER = "<unset>"
    }
}
