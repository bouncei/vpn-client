package com.vpnclient.app.domain.model

/**
 * Domain model representing a VPN server node.
 *
 * @property id Unique identifier for the node
 * @property name Human-readable name (e.g., "New York")
 * @property country Country where the node is located
 * @property latencyMs Average latency to this node in milliseconds
 * @property publicKey Public key for secure connection
 * @property endpointIp IP address of the VPN endpoint
 */
data class VpnNode(
    val id: String,
    val name: String,
    val country: String,
    val latencyMs: Int,
    val publicKey: String,
    val endpointIp: String
)
