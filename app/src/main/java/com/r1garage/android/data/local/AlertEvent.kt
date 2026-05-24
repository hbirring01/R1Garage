package com.r1garage.android.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "alert_event")
data class AlertEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "triggered_at") val triggeredAt: Long,
    val kind: String,
    val message: String,
)

@Dao
interface AlertEventDao {
    @Query("SELECT * FROM alert_event ORDER BY triggered_at DESC LIMIT 50")
    fun observeRecent(): Flow<List<AlertEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AlertEvent): Long
}
