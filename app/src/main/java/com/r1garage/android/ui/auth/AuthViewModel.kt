package com.r1garage.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r1garage.android.data.auth.AuthRepository
import com.r1garage.android.data.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    val state: StateFlow<AuthState> = repository.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.state.value)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun signIn(email: String, password: String) {
        _error.value = null
        viewModelScope.launch {
            repository.signIn(email.trim(), password)
                .onFailure { _error.value = it.message ?: "Login failed" }
        }
    }

    fun submitOtp(email: String, otpToken: String, code: String) {
        _error.value = null
        viewModelScope.launch {
            repository.submitOtp(email, otpToken, code.trim())
                .onFailure { _error.value = it.message ?: "OTP verification failed" }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun signOut() = repository.signOut()
}
