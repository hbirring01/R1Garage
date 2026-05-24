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
 * A completed drive — generated when the poller observes the gear leaving
 * DRIVE/REVERSE and the vehicle parking. Fields are populated from the
 * delta between the trip-start and trip-end vehicle snapshots.
 */
@Entity(tableName = "trip")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long,
    @ColumnInfo(name = "distance_mi") val distanceMi: Double,
    @ColumnInfo(name = "kwh_used") val kwhUsed: Double,
    @ColumnInfo(name = "start_lat") val startLat: Double?,
    @ColumnInfo(name = "start_lon") val startLon: Double?,
    @ColumnInfo(name = "end_lat") val endLat: Double?,
    @ColumnInfo(name = "end_lon") val endLon: Double?,
    @ColumnInfo(name = "elevation_gain_ft") val elevationGainFt: Int? = null,
    @ColumnInfo(name = "temp_f") val tempF: Int? = null,
) {
    fun efficiency(): Double = if (kwhUsed > 0.01) distanceMi / kwhUsed else 0.0
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trip ORDER BY started_at DESC")
    fun observeAll(): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip): Long
}
