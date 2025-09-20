package com.vpnclient.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.vpnclient.app.domain.model.User

/**
 * Data Transfer Object for User API responses.
 * Maps JSON response to domain model.
 */
data class UserDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("email")
    val email: String,
    @SerializedName("token")
    val token: String
) {
    /**
     * Convert DTO to domain model.
     * @return User domain object
     */
    fun toDomain(): User {
        return User(
            id = id,
            email = email,
            token = token
        )
    }
}
