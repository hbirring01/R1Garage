package com.r1garage.android.data.auth

/**
 * Top-level auth state observed by the navigation gate.
 *
 *  - [SignedOut]    → show login screen
 *  - [NeedsOtp]     → show OTP screen (carries the otpToken + email round-trip)
 *  - [SignedIn]     → show main tabbed app
 *  - [Submitting]   → in-flight request from either login or OTP screen
 */
sealed interface AuthState {
    data object SignedOut : AuthState
    data object Submitting : AuthState
    data class NeedsOtp(val email: String, val otpToken: String) : AuthState
    data object SignedIn : AuthState
}
