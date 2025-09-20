package com.vpnclient.app.domain.usecase

import com.vpnclient.app.domain.model.User
import com.vpnclient.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user authentication.
 * Encapsulates business logic for login validation and execution.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute login with email and password validation.
     * @param email User's email address
     * @param password User's password
     * @return Result containing User on success, or error on failure
     */
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validate input
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }
        
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        
        // Delegate to repository
        return authRepository.login(email, password)
    }
    
    /**
     * Basic email validation.
     * @param email Email to validate
     * @return True if email format is valid
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
