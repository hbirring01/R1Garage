package com.r1garage.android.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the periodic vehicle poll.
 *
 * **12 V battery safety:** the worker only issues the read-only `vehicleState`
 * GraphQL query, which returns cached cloud telemetry pushed FROM the vehicle
 * while it is already awake. It never opens the live websocket
 * (`subscribeVehicle`) and never sends commands — those are the operations
 * that would wake the car. Polling cadence is therefore a phone-battery
 * concern, not a vehicle concern. We still throttle aggressively in
 * [VehiclePollWorker] when the car is parked & idle so we're not paying for
 * unnecessary cellular wakes on the phone side.
 *
 * Android's WorkManager enforces a 15-minute minimum periodic interval; the
 * worker itself decides whether to actually hit the network on each tick.
 */
@Singleton
class VehiclePollScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedulePeriodic() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<VehiclePollWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /** Stop polling. Call on sign-out so a signed-out app makes no requests. */
    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "r1garage.vehicle_poll"
    }
}
