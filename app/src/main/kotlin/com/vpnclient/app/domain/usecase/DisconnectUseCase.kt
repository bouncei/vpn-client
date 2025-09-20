package com.vpnclient.app.domain.usecase

import com.vpnclient.app.domain.repository.ConnectionRepository
import javax.inject.Inject

/**
 * Use case for disconnecting from VPN.
 * Handles cleanup and state management during disconnection.
 */
class DisconnectUseCase @Inject constructor(
    private val connectionRepository: ConnectionRepository
) {
    /**
     * Disconnect from current VPN connection.
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        return connectionRepository.disconnect()
    }
}
