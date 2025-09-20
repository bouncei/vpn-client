package com.vpnclient.app.domain.connection

import com.vpnclient.app.domain.model.ConnectionState

/**
 * Interface for connection state management.
 * Implements the State pattern for connection lifecycle.
 */
interface ConnectionStateHandler {
    /**
     * Handle connection request in current state.
     * @param context Connection context
     * @param nodeId ID of the node to connect to
     * @param strategy Connection strategy to use
     * @return Result of the connection attempt
     */
    suspend fun connect(
        context: ConnectionContext,
        nodeId: String,
        strategy: ConnectionStrategy
    ): Result<Unit>

    /**
     * Handle disconnection request in current state.
     * @param context Connection context
     * @return Result of the disconnection attempt
     */
    suspend fun disconnect(context: ConnectionContext): Result<Unit>

    /**
     * Get the current connection state.
     * @return Current ConnectionState
     */
    fun getState(): ConnectionState
}

/**
 * Context class for managing connection state transitions.
 * Maintains current state and handles state changes.
 */
class ConnectionContext {
    private var currentHandler: ConnectionStateHandler = DisconnectedStateHandler()

    /**
     * Set the current state handler.
     * @param handler New state handler
     */
    fun setState(handler: ConnectionStateHandler) {
        currentHandler = handler
    }

    /**
     * Get the current state handler.
     * @return Current state handler
     */
    fun getCurrentHandler(): ConnectionStateHandler = currentHandler

    /**
     * Connect to a VPN node using the current state handler.
     * @param nodeId ID of the node to connect to
     * @param strategy Connection strategy to use
     * @return Result of the connection attempt
     */
    suspend fun connect(nodeId: String, strategy: ConnectionStrategy): Result<Unit> {
        return currentHandler.connect(this, nodeId, strategy)
    }

    /**
     * Disconnect from VPN using the current state handler.
     * @return Result of the disconnection attempt
     */
    suspend fun disconnect(): Result<Unit> {
        return currentHandler.disconnect(this)
    }

    /**
     * Get the current connection state.
     * @return Current ConnectionState
     */
    fun getState(): ConnectionState = currentHandler.getState()
}

/**
 * State handler for disconnected state.
 */
class DisconnectedStateHandler : ConnectionStateHandler {
    override suspend fun connect(
        context: ConnectionContext,
        nodeId: String,
        strategy: ConnectionStrategy
    ): Result<Unit> {
        // Transition to connecting state
        context.setState(ConnectingStateHandler(nodeId, strategy))
        return Result.success(Unit)
    }

    override suspend fun disconnect(context: ConnectionContext): Result<Unit> {
        return Result.failure(IllegalStateException("Already disconnected"))
    }

    override fun getState(): ConnectionState = ConnectionState.Disconnected
}

/**
 * State handler for connecting state.
 */
class ConnectingStateHandler(
    private val nodeId: String,
    private val strategy: ConnectionStrategy
) : ConnectionStateHandler {
    
    override suspend fun connect(
        context: ConnectionContext,
        nodeId: String,
        strategy: ConnectionStrategy
    ): Result<Unit> {
        return Result.failure(IllegalStateException("Already connecting"))
    }

    override suspend fun disconnect(context: ConnectionContext): Result<Unit> {
        // Cancel connection and return to disconnected
        context.setState(DisconnectedStateHandler())
        return Result.success(Unit)
    }

    override fun getState(): ConnectionState = ConnectionState.Connecting(nodeId, 0f)
}

/**
 * State handler for connected state.
 */
class ConnectedStateHandler(
    private val nodeId: String,
    private val connectedAt: Long
) : ConnectionStateHandler {
    
    override suspend fun connect(
        context: ConnectionContext,
        nodeId: String,
        strategy: ConnectionStrategy
    ): Result<Unit> {
        return Result.failure(IllegalStateException("Already connected. Disconnect first."))
    }

    override suspend fun disconnect(context: ConnectionContext): Result<Unit> {
        // Transition to disconnecting state
        context.setState(DisconnectingStateHandler(nodeId))
        return Result.success(Unit)
    }

    override fun getState(): ConnectionState = ConnectionState.Connected(
        nodeId = nodeId,
        connectedAt = connectedAt,
        duration = System.currentTimeMillis() - connectedAt
    )
}

/**
 * State handler for disconnecting state.
 */
class DisconnectingStateHandler(
    private val nodeId: String
) : ConnectionStateHandler {
    
    override suspend fun connect(
        context: ConnectionContext,
        nodeId: String,
        strategy: ConnectionStrategy
    ): Result<Unit> {
        return Result.failure(IllegalStateException("Currently disconnecting"))
    }

    override suspend fun disconnect(context: ConnectionContext): Result<Unit> {
        return Result.failure(IllegalStateException("Already disconnecting"))
    }

    override fun getState(): ConnectionState = ConnectionState.Disconnecting(nodeId)
}

/**
 * State handler for error state.
 */
class ErrorStateHandler(
    private val nodeId: String?,
    private val error: String
) : ConnectionStateHandler {
    
    override suspend fun connect(
        context: ConnectionContext,
        nodeId: String,
        strategy: ConnectionStrategy
    ): Result<Unit> {
        // Allow retry from error state
        context.setState(ConnectingStateHandler(nodeId, strategy))
        return Result.success(Unit)
    }

    override suspend fun disconnect(context: ConnectionContext): Result<Unit> {
        // Clear error and return to disconnected
        context.setState(DisconnectedStateHandler())
        return Result.success(Unit)
    }

    override fun getState(): ConnectionState = ConnectionState.Error(nodeId, error)
}
