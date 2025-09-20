package com.vpnclient.app.domain.repository

import com.vpnclient.app.domain.model.ConnectionSession
import com.vpnclient.app.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for VPN connection management.
 * Handles connection state persistence and session tracking.
 */
interface ConnectionRepository {
    /**
     * Initiate connection to a VPN node.
     * @param nodeId ID of the node to connect to
     * @param strategy Connection strategy to use ("fast" or "secure")
     * @return Result indicating success or failure
     */
    suspend fun connect(nodeId: String, strategy: String = "fast"): Result<Unit>

    /**
     * Disconnect from current VPN connection.
     * @return Result indicating success or failure
     */
    suspend fun disconnect(): Result<Unit>

    /**
     * Observe the current connection state.
     * @return Flow emitting connection state changes
     */
    fun observeConnectionState(): Flow<ConnectionState>

    /**
     * Get the current active connection session.
     * @return Current session if active, null otherwise
     */
    suspend fun getCurrentSession(): ConnectionSession?

    /**
     * Get connection history for the current user.
     * @return List of previous connection sessions
     */
    suspend fun getConnectionHistory(): List<ConnectionSession>

    /**
     * Save a connection session to persistent storage.
     * @param session Session to save
     */
    suspend fun saveSession(session: ConnectionSession)
}
