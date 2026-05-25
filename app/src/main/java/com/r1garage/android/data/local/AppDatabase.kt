package com.r1garage.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Trip::class,
        ChargeSession::class,
        Mod::class,
        AlertEvent::class,
        VehicleSnapshotEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun chargeSessionDao(): ChargeSessionDao
    abstract fun modDao(): ModDao
    abstract fun alertEventDao(): AlertEventDao
    abstract fun vehicleSnapshotDao(): VehicleSnapshotDao

    companion object {
        /** v2: persist vehicle `power_state` on each snapshot. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicle_snapshot ADD COLUMN power_state TEXT")
            }
        }
    }
}
