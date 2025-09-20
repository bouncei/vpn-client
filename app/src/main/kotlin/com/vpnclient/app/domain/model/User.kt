package com.vpnclient.app.domain.model

/**
 * Domain model representing a user in the VPN system.
 *
 * @property id Unique identifier for the user
 * @property email User's email address
 * @property token Authentication token for API requests
 */
data class User(
    val id: Long,
    val email: String,
    val token: String
)
