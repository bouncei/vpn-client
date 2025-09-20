package com.vpnclient.app.domain.repository

import com.vpnclient.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Follows the Repository pattern to abstract data access.
 */
interface AuthRepository {
    /**
     * Authenticate user with email and password.
     * @param email User's email address
     * @param password User's password
     * @return Result containing User object on success, or error on failure
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Log out the current user and clear stored credentials.
     */
    suspend fun logout()

    /**
     * Get the currently stored authentication token.
     * @return Token string if available, null otherwise
     */
    suspend fun getStoredToken(): String?

    /**
     * Get the currently authenticated user.
     * @return Flow emitting the current user or null if not authenticated
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Check if user is currently authenticated.
     * @return Flow emitting authentication status
     */
    fun isAuthenticated(): Flow<Boolean>
}
