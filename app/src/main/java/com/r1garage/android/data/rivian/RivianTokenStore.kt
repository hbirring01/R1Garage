package com.r1garage.android.data.rivian

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted on-device store for Rivian session tokens.
 *
 * Header mapping for the consumer GraphQL gateway:
 *  - `Csrf-Token`        ← [csrfToken]
 *  - `A-Sess`            ← [appSessionToken]
 *  - `U-Sess`            ← [userSessionToken]
 *
 * [accessToken] and [refreshToken] are returned by Rivian's login mutation
 * but are not used as request headers themselves on the consumer GraphQL
 * endpoint — they're kept here for a future silent-refresh flow.
 *
 * [userEmail] is kept because Rivian's OTP mutation requires re-sending it
 * alongside the otpToken.
 */
@Singleton
class RivianTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "r1_garage_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var csrfToken: String?
        get() = prefs.getString(KEY_CSRF, null)
        set(value) { prefs.edit().putString(KEY_CSRF, value).apply() }

    var appSessionToken: String?
        get() = prefs.getString(KEY_APP_SESSION, null)
        set(value) { prefs.edit().putString(KEY_APP_SESSION, value).apply() }

    var userSessionToken: String?
        get() = prefs.getString(KEY_USER_SESSION, null)
        set(value) { prefs.edit().putString(KEY_USER_SESSION, value).apply() }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) { prefs.edit().putString(KEY_ACCESS, value).apply() }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) { prefs.edit().putString(KEY_REFRESH, value).apply() }

    var userEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) { prefs.edit().putString(KEY_EMAIL, value).apply() }

    /**
     * Discovered once per session via `GetUserInfo`. Every vehicle-scoped
     * query needs it. Not encrypted-sensitive on its own, but kept here for
     * lifecycle parity with the rest of the session.
     */
    var vehicleId: String?
        get() = prefs.getString(KEY_VEHICLE_ID, null)
        set(value) { prefs.edit().putString(KEY_VEHICLE_ID, value).apply() }

    /** Friendly vehicle name (user-set in Rivian app) or model fallback. */
    var vehicleName: String?
        get() = prefs.getString(KEY_VEHICLE_NAME, null)
        set(value) { prefs.edit().putString(KEY_VEHICLE_NAME, value).apply() }

    /** True when we have enough tokens to make an authenticated consumer call. */
    val isSignedIn: Boolean
        get() = !userSessionToken.isNullOrBlank() &&
            !appSessionToken.isNullOrBlank() &&
            !csrfToken.isNullOrBlank()

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_CSRF = "csrf_token"
        const val KEY_APP_SESSION = "app_session_token"
        const val KEY_USER_SESSION = "user_session_token"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_EMAIL = "user_email"
        const val KEY_VEHICLE_ID = "vehicle_id"
        const val KEY_VEHICLE_NAME = "vehicle_name"
    }
}
