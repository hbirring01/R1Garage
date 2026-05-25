package com.r1garage.android.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "r1_garage_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val lowPowerModeKey = booleanPreferencesKey("low_power_mode")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        runCatching { ThemeMode.valueOf(prefs[themeModeKey] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeModeKey] = mode.name }
    }

    /**
     * Low-power mode collapses idle polling to ~4 h instead of ~1 h. The
     * vehicle is unaffected either way (we only ever issue read-only
     * `vehicleState` queries) — this is a phone-battery / data toggle.
     */
    val lowPowerMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[lowPowerModeKey] ?: false
    }

    /** Suspend snapshot read for use inside a Worker. */
    suspend fun lowPowerModeOnce(): Boolean = lowPowerMode.first()

    suspend fun setLowPowerMode(enabled: Boolean) {
        context.dataStore.edit { it[lowPowerModeKey] = enabled }
    }
}
