package com.vpnclient.app.presentation.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.vpnclient.app.domain.model.User
import com.vpnclient.app.domain.usecase.LoginUseCase
import com.vpnclient.app.domain.usecase.LogoutUseCase
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
 * Tests core functionality without complex coroutine timing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        logoutUseCase = mockk()
        viewModel = AuthViewModel(loginUseCase, logoutUseCase)
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
        assertFalse("Should not be loading", state.isLoading)
        assertTrue("Should be authenticated", state.isAuthenticated)
        assertNull("Error should be null", state.error)
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
        assertFalse("Should not be loading", state.isLoading)
        assertFalse("Should not be authenticated", state.isAuthenticated)
        assertEquals("Error message should match", "Invalid credentials", state.error)
    }

    @Test
    fun `clearError clears error message`() = runTest {
        // Given - Set an error first by mocking a failed login
        val email = "test@vpn.com"
        val password = "wrong_password"
        val error = Exception("Invalid credentials")
        
        coEvery { loginUseCase(email, password) } returns Result.failure(error)
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        
        // Simulate error state
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error is set
        val errorState = viewModel.uiState.value
        assertEquals("Error should be set", "Invalid credentials", errorState.error)

        // When
        viewModel.clearError()

        // Then
        val clearedState = viewModel.uiState.value
        assertNull("Error should be cleared", clearedState.error)
    }

    @Test
    fun `logout clears authentication state`() = runTest {
        // Given - User is authenticated
        val email = "test@vpn.com"
        val password = "password123"
        val user = User(1L, email, "mock_token")
        
        coEvery { loginUseCase(email, password) } returns Result.success(user)
        coEvery { logoutUseCase() } returns Result.success(Unit)
        
        // Login first
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.login(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify authenticated
        val authenticatedState = viewModel.uiState.value
        assertTrue("Should be authenticated", authenticatedState.isAuthenticated)

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val loggedOutState = viewModel.uiState.value
        assertFalse("Should not be authenticated", loggedOutState.isAuthenticated)
        assertEquals("Email should be cleared", "", loggedOutState.email)
        assertEquals("Password should be cleared", "", loggedOutState.password)
        assertNull("Error should be null", loggedOutState.error)
    }
}
