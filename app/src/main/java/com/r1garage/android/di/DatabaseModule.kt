package com.r1garage.android.di

import android.content.Context
import androidx.room.Room
import com.r1garage.android.data.local.AlertEventDao
import com.r1garage.android.data.local.AppDatabase
import com.r1garage.android.data.local.ChargeSessionDao
import com.r1garage.android.data.local.ModDao
import com.r1garage.android.data.local.TripDao
import com.r1garage.android.data.local.VehicleSnapshotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "r1_garage.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides fun provideTripDao(db: AppDatabase): TripDao = db.tripDao()
    @Provides fun provideChargeSessionDao(db: AppDatabase): ChargeSessionDao = db.chargeSessionDao()
    @Provides fun provideModDao(db: AppDatabase): ModDao = db.modDao()
    @Provides fun provideAlertEventDao(db: AppDatabase): AlertEventDao = db.alertEventDao()
    @Provides fun provideVehicleSnapshotDao(db: AppDatabase): VehicleSnapshotDao =
        db.vehicleSnapshotDao()
}
