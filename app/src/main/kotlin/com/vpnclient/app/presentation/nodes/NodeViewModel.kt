package com.vpnclient.app.presentation.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpnclient.app.domain.model.ConnectionState
import com.vpnclient.app.domain.model.VpnNode
import com.vpnclient.app.domain.usecase.ConnectToNodeUseCase
import com.vpnclient.app.domain.usecase.DisconnectUseCase
import com.vpnclient.app.domain.usecase.GetConnectionStatusUseCase
import com.vpnclient.app.domain.usecase.GetNodesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for VPN nodes list screen.
 * Manages node fetching, connection state, and user interactions.
 */
@HiltViewModel
class NodeViewModel @Inject constructor(
    private val getNodesUseCase: GetNodesUseCase,
    private val connectToNodeUseCase: ConnectToNodeUseCase,
    private val disconnectUseCase: DisconnectUseCase,
    private val getConnectionStatusUseCase: GetConnectionStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NodeUiState())
    val uiState: StateFlow<NodeUiState> = _uiState.asStateFlow()

    init {
        loadNodes()
        observeConnectionState()
    }

    /**
     * Load VPN nodes from repository.
     * @param forceRefresh Whether to force refresh from network
     */
    fun loadNodes(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            getNodesUseCase(forceRefresh)
                .onSuccess { nodes ->
                    _uiState.value = _uiState.value.copy(
                        nodes = nodes,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        nodes = emptyList(),
                        isLoading = false,
                        error = exception.message ?: "Failed to load nodes"
                    )
                }
        }
    }

    /**
     * Connect to a specific VPN node.
     * @param nodeId ID of the node to connect to
     * @param strategy Connection strategy ("fast" or "secure")
     */
    fun connectToNode(nodeId: String, strategy: String = "fast") {
        viewModelScope.launch {
            connectToNodeUseCase(nodeId, strategy)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Connection failed"
                    )
                }
        }
    }

    /**
     * Disconnect from current VPN connection.
     */
    fun disconnect() {
        viewModelScope.launch {
            disconnectUseCase()
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Disconnection failed"
                    )
                }
        }
    }

    /**
     * Refresh nodes list.
     */
    fun refreshNodes() {
        loadNodes(forceRefresh = true)
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Observe connection state changes and update UI accordingly.
     */
    private fun observeConnectionState() {
        viewModelScope.launch {
            combine(
                getConnectionStatusUseCase(),
                _uiState
            ) { connectionState, currentState ->
                currentState.copy(connectionState = connectionState)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}

/**
 * UI state for nodes screen.
 */
data class NodeUiState(
    val nodes: List<VpnNode> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /**
     * Get the currently connected node ID if any.
     */
    val connectedNodeId: String?
        get() = when (connectionState) {
            is ConnectionState.Connected -> connectionState.nodeId
            is ConnectionState.Connecting -> connectionState.nodeId
            is ConnectionState.Disconnecting -> connectionState.nodeId
            else -> null
        }

    /**
     * Check if currently connecting to a specific node.
     */
    fun isConnectingToNode(nodeId: String): Boolean {
        return connectionState is ConnectionState.Connecting && 
               connectionState.nodeId == nodeId
    }

    /**
     * Check if currently connected to a specific node.
     */
    fun isConnectedToNode(nodeId: String): Boolean {
        return connectionState is ConnectionState.Connected && 
               connectionState.nodeId == nodeId
    }

    /**
     * Check if can connect to nodes (not currently connecting/disconnecting).
     */
    val canConnect: Boolean
        get() = connectionState is ConnectionState.Disconnected || 
                connectionState is ConnectionState.Error
}
