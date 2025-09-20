package com.vpnclient.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for login API requests.
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
