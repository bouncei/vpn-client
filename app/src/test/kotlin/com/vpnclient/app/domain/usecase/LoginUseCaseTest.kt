package com.vpnclient.app.domain.usecase

import com.vpnclient.app.domain.model.User
import com.vpnclient.app.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoginUseCase.
 * Tests business logic validation and repository interaction.
 */
class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `login with valid credentials returns success`() = runTest {
        // Given
        val email = "test@vpn.com"
        val password = "password123"
        val expectedUser = User(1L, email, "mock_token")
        
        coEvery { authRepository.login(email, password) } returns Result.success(expectedUser)

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }

    @Test
    fun `login with empty email returns failure`() = runTest {
        // Given
        val email = ""
        val password = "password123"

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Email cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with empty password returns failure`() = runTest {
        // Given
        val email = "test@vpn.com"
        val password = ""

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Password cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with invalid email format returns failure`() = runTest {
        // Given
        val email = "invalid-email"
        val password = "password123"

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid email format", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with repository failure returns failure`() = runTest {
        // Given
        val email = "test@vpn.com"
        val password = "password123"
        val expectedError = Exception("Network error")
        
        coEvery { authRepository.login(email, password) } returns Result.failure(expectedError)

        // When
        val result = loginUseCase(email, password)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }
}
