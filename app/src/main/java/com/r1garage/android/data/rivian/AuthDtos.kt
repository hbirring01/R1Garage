package com.r1garage.android.data.rivian

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Mutation response wrappers ---------------------------------------

@Serializable
data class CreateCsrfTokenData(val createCsrfToken: CsrfPayload? = null)

@Serializable
data class CsrfPayload(
    val csrfToken: String? = null,
    val appSessionToken: String? = null,
)

@Serializable
data class LoginData(val login: LoginPayload? = null)

@Serializable
data class LoginWithOtpData(val loginWithOTP: LoginPayload? = null)

/**
 * Flattened union of [`MobileLoginResponse`] and [`MobileMFALoginResponse`].
 * Discriminate on [typename] at the call site.
 *
 *  - `MobileLoginResponse`     → has [accessToken], [refreshToken], [userSessionToken]
 *  - `MobileMFALoginResponse`  → has [otpToken] (caller must then prompt for OTP)
 */
@Serializable
data class LoginPayload(
    @SerialName("__typename") val typename: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val userSessionToken: String? = null,
    val otpToken: String? = null,
)
