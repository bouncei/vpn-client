package com.vpnclient.app.domain.usecase

import com.vpnclient.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case for user logout.
 * Encapsulates business logic for logout and cleanup.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Execute logout and clear all stored authentication data.
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
