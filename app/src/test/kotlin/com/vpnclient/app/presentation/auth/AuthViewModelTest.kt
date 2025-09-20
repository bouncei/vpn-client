package com.vpnclient.app.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vpnclient.app.domain.model.User
import com.vpnclient.app.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for AuthViewModel.
 * Tests UI state management and user interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        viewModel = AuthViewModel(loginUseCase)
    }

    @Test
    fun `initial state is correct`() {
        // Given - ViewModel is initialized

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertNull(state.error)
        assertFalse(state.isFormValid)
    }

    @Test
    fun `updateEmail updates state correctly`() {
        // Given
        val newEmail = "test@vpn.com"

        // When
        viewModel.updateEmail(newEmail)

        // Then
        val state = viewModel.uiState.value
        assertEquals(newEmail, state.email)
        assertNull(state.error) // Error should be cleared
    }

    @Test
    fun `updatePassword updates state correctly`() {
        // Given
        val newPassword = "password123"

        // When
        viewModel.updatePassword(newPassword)

        // Then
        val state = viewModel.uiState.value
        assertEquals(newPassword, state.password)
        assertNull(state.error) // Error should be cleared
    }

    @Test
    fun `isFormValid returns true when both fields are filled`() {
        // Given
        viewModel.updateEmail("test@vpn.com")
        viewModel.updatePassword("password123")

        // When
        val state = viewModel.uiState.value

        // Then
        assertTrue(state.isFormValid)
    }

    @Test
    fun `login success updates state correctly`() = runTest {
        // Given
        val email = "test@vpn.com"
        val password = "password123"
        val user = User(1L, email, "mock_token")
        
        coEvery { loginUseCase(email, password) } returns Result.success(user)

        // When
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertNull(state.error)
    }

    @Test
    fun `login failure updates state correctly`() = runTest {
        // Given
        val email = "test@vpn.com"
        val password = "wrong_password"
        val error = Exception("Invalid credentials")
        
        coEvery { loginUseCase(email, password) } returns Result.failure(error)

        // When
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertEquals("Invalid credentials", state.error)
    }

    @Test
    fun `login sets loading state during execution`() = runTest {
        // Given
        val email = "test@vpn.com"
        val password = "password123"
        
        coEvery { loginUseCase(email, password) } returns Result.success(
            User(1L, email, "mock_token")
        )

        // When
        viewModel.login(email, password)

        // Then - Check loading state before completion
        val loadingState = viewModel.uiState.value
        assertTrue(loadingState.isLoading)
        assertNull(loadingState.error)
    }

    @Test
    fun `clearError clears error message`() {
        // Given - Set an error first
        viewModel.updateEmail("test@vpn.com")
        viewModel.updatePassword("password123")
        
        // Simulate error state (normally set by login failure)
        viewModel.login("test@vpn.com", "wrong_password")

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.error)
    }
}
