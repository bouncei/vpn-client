package com.vpnclient.app.domain.model

/**
 * Domain model representing an active or historical VPN connection session.
 *
 * @property id Unique identifier for the session
 * @property userId ID of the user who owns this session
 * @property nodeId ID of the VPN node for this session
 * @property connectedAt Timestamp when the connection was established
 * @property disconnectedAt Timestamp when the connection was terminated (null if still active)
 * @property isActive Whether this session is currently active
 */
data class ConnectionSession(
    val id: Long,
    val userId: Long,
    val nodeId: String,
    val connectedAt: Long,
    val disconnectedAt: Long? = null,
    val isActive: Boolean = true
)
