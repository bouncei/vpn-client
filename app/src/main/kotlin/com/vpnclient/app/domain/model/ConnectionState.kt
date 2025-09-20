package com.vpnclient.app.domain.model

/**
 * Sealed class representing the various states of VPN connection.
 * Implements the State pattern for connection lifecycle management.
 */
sealed class ConnectionState {
    /**
     * No active VPN connection.
     */
    object Disconnected : ConnectionState()

    /**
     * Currently establishing connection to a VPN node.
     * @property nodeId ID of the node being connected to
     * @property progress Connection progress (0.0 to 1.0)
     */
    data class Connecting(
        val nodeId: String,
        val progress: Float = 0f
    ) : ConnectionState()

    /**
     * Successfully connected to a VPN node.
     * @property nodeId ID of the connected node
     * @property connectedAt Timestamp when connection was established
     * @property duration Current connection duration in milliseconds
     */
    data class Connected(
        val nodeId: String,
        val connectedAt: Long,
        val duration: Long = 0L
    ) : ConnectionState()

    /**
     * Currently disconnecting from VPN.
     * @property nodeId ID of the node being disconnected from
     */
    data class Disconnecting(
        val nodeId: String
    ) : ConnectionState()

    /**
     * Connection failed due to an error.
     * @property nodeId ID of the node that failed to connect
     * @property error Error message describing the failure
     */
    data class Error(
        val nodeId: String?,
        val error: String
    ) : ConnectionState()
}
