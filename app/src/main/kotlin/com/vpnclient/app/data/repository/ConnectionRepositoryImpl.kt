package com.vpnclient.app.data.repository

import com.vpnclient.app.data.local.datastore.SessionDataStore
import com.vpnclient.app.domain.connection.ConnectionManager
import com.vpnclient.app.domain.model.ConnectionSession
import com.vpnclient.app.domain.model.ConnectionState
import com.vpnclient.app.domain.repository.ConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ConnectionRepository using ConnectionManager.
 * Manages connection state and session persistence.
 */
@Singleton
class ConnectionRepositoryImpl @Inject constructor(
    private val connectionManager: ConnectionManager,
    private val sessionDataStore: SessionDataStore
) : ConnectionRepository {

    private val _connectionHistory = mutableListOf<ConnectionSession>()

    override suspend fun connect(nodeId: String, strategy: String): Result<Unit> {
        return try {
            val result = connectionManager.connect(nodeId, strategy)
            
            if (result.isSuccess) {
                // Create and save session when connection starts
                val session = ConnectionSession(
                    id = System.currentTimeMillis(),
                    userId = 1L, // TODO: Get from auth context
                    nodeId = nodeId,
                    connectedAt = System.currentTimeMillis(),
                    isActive = true
                )
                
                sessionDataStore.saveSession(session)
                _connectionHistory.add(session)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disconnect(): Result<Unit> {
        return try {
            val result = connectionManager.disconnect()
            
            if (result.isSuccess) {
                // Update session as disconnected
                sessionDataStore.disconnectSession()
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeConnectionState(): Flow<ConnectionState> {
        return connectionManager.connectionState
    }

    override suspend fun getCurrentSession(): ConnectionSession? {
        return sessionDataStore.getCurrentSession().first()
    }

    override suspend fun getConnectionHistory(): List<ConnectionSession> {
        return _connectionHistory.toList()
    }

    override suspend fun saveSession(session: ConnectionSession) {
        sessionDataStore.saveSession(session)
        _connectionHistory.add(session)
    }
}
