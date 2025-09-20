package com.vpnclient.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpnclient.app.domain.usecase.LoginUseCase
import com.vpnclient.app.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication screen.
 * Manages UI state and handles login business logic.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Handle login attempt with email and password.
     * @param email User's email address
     * @param password User's password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        error = exception.message ?: "Login failed"
                    )
                }
        }
    }

    /**
     * Update email field value.
     * @param email New email value
     */
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            error = null // Clear error when user starts typing
        )
    }

    /**
     * Update password field value.
     * @param password New password value
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            error = null // Clear error when user starts typing
        )
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Handle user logout.
     * Clears authentication state and stored credentials.
     */
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = false,
                        email = "",
                        password = "",
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Logout failed"
                    )
                }
        }
    }
}

/**
 * UI state for authentication screen.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
) {
    /**
     * Check if login form is valid for submission.
     */
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.isNotBlank()
}
