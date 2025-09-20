package com.vpnclient.app.domain.usecase

import com.vpnclient.app.domain.model.ConnectionState
import com.vpnclient.app.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing VPN connection status.
 * Provides real-time connection state updates to the UI.
 */
class GetConnectionStatusUseCase @Inject constructor(
    private val connectionRepository: ConnectionRepository
) {
    /**
     * Observe current connection state with real-time updates.
     * @return Flow emitting connection state changes
     */
    operator fun invoke(): Flow<ConnectionState> {
        return connectionRepository.observeConnectionState()
    }
    
    /**
     * Get current connection session if active.
     * @return Current session or null if not connected
     */
    suspend fun getCurrentSession() = connectionRepository.getCurrentSession()
    
    /**
     * Get connection history for analytics and user reference.
     * @return List of previous connection sessions
     */
    suspend fun getConnectionHistory() = connectionRepository.getConnectionHistory()
}
