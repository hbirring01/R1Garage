package com.r1garage.android.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "mod")
data class Mod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "installed_at") val installedAt: Long,
    @ColumnInfo(name = "odometer_mi") val odometerMi: Int,
    @ColumnInfo(name = "cost_usd") val costUsd: Double? = null,
    val notes: String? = null,
)

@Dao
interface ModDao {
    @Query("SELECT * FROM mod ORDER BY installed_at DESC")
    fun observeAll(): Flow<List<Mod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mod: Mod): Long

    @Update
    suspend fun update(mod: Mod)

    @Delete
    suspend fun delete(mod: Mod)
}
