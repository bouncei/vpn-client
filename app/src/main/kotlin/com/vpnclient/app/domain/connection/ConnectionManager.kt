package com.vpnclient.app.domain.connection

import com.vpnclient.app.domain.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton manager for VPN connections.
 * Orchestrates connection lifecycle using State and Strategy patterns.
 */
@Singleton
class ConnectionManager @Inject constructor() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionContext = ConnectionContext()
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var currentStrategy: ConnectionStrategy? = null

    init {
        // Start observing connection state changes
        observeConnectionState()
    }

    /**
     * Connect to a VPN node with specified strategy.
     * @param nodeId ID of the node to connect to
     * @param strategyName Name of the connection strategy
     * @return Result indicating success or failure
     */
    suspend fun connect(nodeId: String, strategyName: String): Result<Unit> {
        return try {
            val strategy = ConnectionStrategyFactory.createStrategy(strategyName)
            currentStrategy = strategy

            // Attempt connection using state pattern
            val result = connectionContext.connect(nodeId, strategy)
            
            if (result.isSuccess) {
                // Start connection simulation
                simulateConnection(nodeId, strategy)
            }
            
            result
        } catch (e: Exception) {
            connectionContext.setState(ErrorStateHandler(nodeId, e.message ?: "Connection failed"))
            updateStateFlow()
            Result.failure(e)
        }
    }

    /**
     * Disconnect from current VPN connection.
     * @return Result indicating success or failure
     */
    suspend fun disconnect(): Result<Unit> {
        return try {
            val result = connectionContext.disconnect()
            
            if (result.isSuccess) {
                // Start disconnection simulation
                simulateDisconnection()
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current connection state.
     * @return Current ConnectionState
     */
    fun getCurrentState(): ConnectionState = connectionContext.getState()

    /**
     * Simulate connection process with progress updates.
     * @param nodeId ID of the node being connected to
     * @param strategy Connection strategy being used
     */
    private fun simulateConnection(nodeId: String, strategy: ConnectionStrategy) {
        scope.launch {
            try {
                val steps = strategy.getConnectionSteps()
                val stepDelay = strategy.getStepDelay()

                for ((index, step) in steps.withIndex()) {
                    // Update progress
                    val progress = (index + 1).toFloat() / steps.size
                    connectionContext.setState(ConnectingStateHandler(nodeId, strategy))
                    _connectionState.value = ConnectionState.Connecting(nodeId, progress)

                    // Simulate step delay
                    delay(stepDelay)
                }

                // Connection successful
                val connectedAt = System.currentTimeMillis()
                connectionContext.setState(ConnectedStateHandler(nodeId, connectedAt))
                updateStateFlow()

            } catch (e: Exception) {
                // Connection failed
                connectionContext.setState(ErrorStateHandler(nodeId, e.message ?: "Connection failed"))
                updateStateFlow()
            }
        }
    }

    /**
     * Simulate disconnection process.
     */
    private fun simulateDisconnection() {
        scope.launch {
            try {
                val currentState = connectionContext.getState()
                val nodeId = when (currentState) {
                    is ConnectionState.Connected -> currentState.nodeId
                    is ConnectionState.Connecting -> currentState.nodeId
                    else -> null
                }

                if (nodeId != null) {
                    connectionContext.setState(DisconnectingStateHandler(nodeId))
                    updateStateFlow()

                    // Simulate disconnection delay
                    delay(1000L)
                }

                // Disconnection complete
                connectionContext.setState(DisconnectedStateHandler())
                updateStateFlow()

            } catch (e: Exception) {
                // Force disconnection on error
                connectionContext.setState(DisconnectedStateHandler())
                updateStateFlow()
            }
        }
    }

    /**
     * Observe connection state changes and update StateFlow.
     */
    private fun observeConnectionState() {
        scope.launch {
            while (true) {
                updateStateFlow()
                delay(1000L) // Update every second for duration tracking
            }
        }
    }

    /**
     * Update the StateFlow with current connection state.
     */
    private fun updateStateFlow() {
        _connectionState.value = connectionContext.getState()
    }

    /**
     * Get the current connection strategy being used.
     * @return Current strategy or null if not connecting/connected
     */
    fun getCurrentStrategy(): ConnectionStrategy? = currentStrategy

    /**
     * Check if currently connected to a specific node.
     * @param nodeId ID of the node to check
     * @return True if connected to the specified node
     */
    fun isConnectedToNode(nodeId: String): Boolean {
        val state = getCurrentState()
        return state is ConnectionState.Connected && state.nodeId == nodeId
    }

    /**
     * Check if currently connecting to a specific node.
     * @param nodeId ID of the node to check
     * @return True if connecting to the specified node
     */
    fun isConnectingToNode(nodeId: String): Boolean {
        val state = getCurrentState()
        return state is ConnectionState.Connecting && state.nodeId == nodeId
    }
}
