package com.r1garage.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.r1garage.android.data.auth.AuthState

/**
 * Top-level auth gate. Switches between Login, OTP, and the signed-in
 * app content based on [AuthState]. Hosting screen calls
 * [signedInContent] when fully authenticated.
 */
@Composable
fun AuthGate(
    signedInContent: @Composable () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val s = state) {
            AuthState.SignedIn -> signedInContent()
            AuthState.Submitting -> LoginScreen(
                submitting = true,
                errorMessage = error,
                onSignIn = { _, _ -> /* disabled while submitting */ },
            )
            AuthState.SignedOut -> LoginScreen(
                submitting = false,
                errorMessage = error,
                onSignIn = viewModel::signIn,
            )
            is AuthState.NeedsOtp -> OtpScreen(
                email = s.email,
                submitting = false,
                errorMessage = error,
                onSubmit = { code -> viewModel.submitOtp(s.email, s.otpToken, code) },
                onCancel = {
                    viewModel.signOut()
                    viewModel.clearError()
                },
            )
        }
    }
}
