package com.r1garage.android.data.rivian

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted on-device store for Rivian session tokens. Uses
 * androidx.security.crypto-backed shared preferences so the access token
 * never lands on disk in plain text. There is no Rivian-issued refresh
 * token on the public/unofficial endpoint, so we expose a single getter
 * and setter.
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

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) { prefs.edit().putString(KEY_ACCESS, value).apply() }

    var csrfToken: String?
        get() = prefs.getString(KEY_CSRF, null)
        set(value) { prefs.edit().putString(KEY_CSRF, value).apply() }

    var appSessionToken: String?
        get() = prefs.getString(KEY_APP_SESSION, null)
        set(value) { prefs.edit().putString(KEY_APP_SESSION, value).apply() }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_CSRF = "csrf_token"
        const val KEY_APP_SESSION = "app_session_token"
    }
}
