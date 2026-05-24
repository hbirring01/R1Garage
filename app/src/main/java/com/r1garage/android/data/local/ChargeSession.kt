package com.r1garage.android.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "charge_session")
data class ChargeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long,
    @ColumnInfo(name = "start_soc") val startSoc: Int,
    @ColumnInfo(name = "end_soc") val endSoc: Int,
    @ColumnInfo(name = "kwh_added") val kwhAdded: Double,
    @ColumnInfo(name = "peak_kw") val peakKw: Double,
    @ColumnInfo(name = "lat") val lat: Double?,
    @ColumnInfo(name = "lon") val lon: Double?,
    @ColumnInfo(name = "cost_usd") val costUsd: Double? = null,
)

@Dao
interface ChargeSessionDao {
    @Query("SELECT * FROM charge_session ORDER BY started_at DESC")
    fun observeAll(): Flow<List<ChargeSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ChargeSession): Long
}
