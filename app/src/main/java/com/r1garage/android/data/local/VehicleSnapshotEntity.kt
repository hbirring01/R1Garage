package com.r1garage.android.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * One row per poll cycle. The current row is what powers the "Now" dashboard;
 * the history table doubles as a forensic record so we can rebuild trip /
 * charge sessions later if detection logic changes.
 */
@Entity(tableName = "vehicle_snapshot")
data class VehicleSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long,
    @ColumnInfo(name = "vehicle_name") val vehicleName: String?,
    @ColumnInfo(name = "soc_pct") val socPct: Int?,
    @ColumnInfo(name = "range_mi") val rangeMi: Int?,
    @ColumnInfo(name = "odometer_mi") val odometerMi: Int?,
    val gear: String?,
    val locked: Boolean?,
    @ColumnInfo(name = "plug_state") val plugState: String?,
    @ColumnInfo(name = "charging_kw") val chargingKw: Double?,
    val lat: Double?,
    val lon: Double?,
    @ColumnInfo(name = "twelve_volt") val twelveVolt: Double?,
)

@Dao
interface VehicleSnapshotDao {
    @Query("SELECT * FROM vehicle_snapshot ORDER BY fetched_at DESC LIMIT 1")
    fun observeLatest(): Flow<VehicleSnapshotEntity?>

    @Query("SELECT * FROM vehicle_snapshot ORDER BY fetched_at DESC LIMIT 1")
    suspend fun latest(): VehicleSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: VehicleSnapshotEntity): Long

    @Query("DELETE FROM vehicle_snapshot WHERE fetched_at < :cutoff")
    suspend fun pruneOlderThan(cutoff: Long)
}
