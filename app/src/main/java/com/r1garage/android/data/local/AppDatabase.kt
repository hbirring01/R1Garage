package com.r1garage.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Trip::class,
        ChargeSession::class,
        Mod::class,
        AlertEvent::class,
        VehicleSnapshotEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun chargeSessionDao(): ChargeSessionDao
    abstract fun modDao(): ModDao
    abstract fun alertEventDao(): AlertEventDao
    abstract fun vehicleSnapshotDao(): VehicleSnapshotDao
}
