package com.r1garage.android.data.auth

import com.r1garage.android.data.rivian.AuthDtosJson
import com.r1garage.android.data.rivian.CreateCsrfTokenData
import com.r1garage.android.data.rivian.GraphQlRequest
import com.r1garage.android.data.rivian.LoginData
import com.r1garage.android.data.rivian.LoginWithOtpData
import com.r1garage.android.data.rivian.RivianAuthApi
import com.r1garage.android.data.rivian.RivianAuthQueries
import com.r1garage.android.data.rivian.RivianTokenStore
import com.r1garage.android.work.VehiclePollScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drives the [AuthState] machine and persists tokens via [RivianTokenStore].
 *
 * Flow:
 *  1. [signIn] → `createCsrfToken` (gateway) → `login` (gateway)
 *  2. If response is `MobileMFALoginResponse`, emit [AuthState.NeedsOtp].
 *  3. [submitOtp] → `loginWithOTP` (gateway) → final tokens stored.
 *  4. Subsequent consumer-API calls use the stored Csrf/A-Sess/U-Sess headers.
 */
@Singleton
class AuthRepository
    @Inject
    constructor(
        private val authApi: RivianAuthApi,
        private val tokenStore: RivianTokenStore,
        private val pollScheduler: VehiclePollScheduler,
    ) {
        private val _state =
            MutableStateFlow<AuthState>(
                if (tokenStore.isSignedIn) AuthState.SignedIn else AuthState.SignedOut,
            )
        val state: StateFlow<AuthState> = _state.asStateFlow()

        suspend fun signIn(
            email: String,
            password: String,
        ): Result<Unit> =
            runCatching {
                require(email.isNotBlank()) { "email required" }
                require(password.isNotBlank()) { "password required" }
                _state.value = AuthState.Submitting

                // 1. CSRF bootstrap — populates Csrf-Token + A-Sess for subsequent calls.
                bootstrapCsrfToken()

                // 2. Login — may immediately succeed OR demand OTP.
                val loginResp =
                    authApi.graphql(
                        GraphQlRequest(
                            operationName = "Login",
                            query = RivianAuthQueries.LOGIN,
                            variables =
                                buildJsonObject {
                                    put("email", JsonPrimitive(email))
                                    put("password", JsonPrimitive(password))
                                },
                        ),
                    )
                loginResp.errors?.firstOrNull()?.let { error("Login failed: ${it.message}") }

                val data = loginResp.data ?: error("Login: empty response body")
                val payload =
                    AuthDtosJson.decodeFromJsonElement<LoginData>(data).login
                        ?: error("Login: no login payload")

                when (payload.typename) {
                    "MobileLoginResponse" -> {
                        persistSession(email, payload.accessToken, payload.refreshToken, payload.userSessionToken)
                        _state.value = AuthState.SignedIn
                    }

                    "MobileMFALoginResponse" -> {
                        val otpToken = payload.otpToken ?: error("MFA response missing otpToken")
                        tokenStore.userEmail = email
                        _state.value = AuthState.NeedsOtp(email = email, otpToken = otpToken)
                    }

                    else -> {
                        error("Unexpected login response __typename=${payload.typename}")
                    }
                }
            }.onFailure { _state.value = AuthState.SignedOut }

        suspend fun submitOtp(
            email: String,
            otpToken: String,
            otpCode: String,
        ): Result<Unit> =
            runCatching {
                require(otpCode.isNotBlank()) { "code required" }
                _state.value = AuthState.Submitting

                val resp =
                    authApi.graphql(
                        GraphQlRequest(
                            operationName = "LoginWithOTP",
                            query = RivianAuthQueries.LOGIN_WITH_OTP,
                            variables =
                                buildJsonObject {
                                    put("email", JsonPrimitive(email))
                                    put("otpCode", JsonPrimitive(otpCode))
                                    put("otpToken", JsonPrimitive(otpToken))
                                },
                        ),
                    )
                resp.errors?.firstOrNull()?.let { error("OTP failed: ${it.message}") }

                val data = resp.data ?: error("OTP: empty response body")
                val payload =
                    AuthDtosJson.decodeFromJsonElement<LoginWithOtpData>(data).loginWithOTP
                        ?: error("OTP: no payload")

                persistSession(email, payload.accessToken, payload.refreshToken, payload.userSessionToken)
                _state.value = AuthState.SignedIn
            }.onFailure {
                // Stay on OTP screen so user can retype; surface error message via the VM.
                val current = _state.value
                if (current is AuthState.Submitting) {
                    _state.value = AuthState.NeedsOtp(email, otpToken)
                }
            }

        fun signOut() {
            // Stop the periodic poller first so no in-flight job races us to
            // make a request with a token we're about to clear.
            pollScheduler.cancel()
            tokenStore.clear()
            _state.value = AuthState.SignedOut
        }

        private suspend fun bootstrapCsrfToken() {
            val resp =
                authApi.graphql(
                    GraphQlRequest(
                        operationName = "CreateCSRFToken",
                        query = RivianAuthQueries.CREATE_CSRF_TOKEN,
                        variables = JsonObject(emptyMap()),
                    ),
                )
            resp.errors?.firstOrNull()?.let { error("CSRF bootstrap failed: ${it.message}") }
            val data = resp.data ?: error("CSRF: empty body")
            val csrf =
                AuthDtosJson.decodeFromJsonElement<CreateCsrfTokenData>(data).createCsrfToken
                    ?: error("CSRF: no payload")
            tokenStore.csrfToken = csrf.csrfToken ?: error("CSRF: missing csrfToken")
            tokenStore.appSessionToken = csrf.appSessionToken ?: error("CSRF: missing appSessionToken")
        }

        private fun persistSession(
            email: String,
            accessToken: String?,
            refreshToken: String?,
            userSessionToken: String?,
        ) {
            tokenStore.userEmail = email
            tokenStore.accessToken = accessToken
            tokenStore.refreshToken = refreshToken
            tokenStore.userSessionToken =
                userSessionToken
                    ?: error("Login response missing userSessionToken")
        }
    }
