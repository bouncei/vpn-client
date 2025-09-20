package com.vpnclient.app.data.remote.api

import com.vpnclient.app.data.remote.dto.LoginRequest
import com.vpnclient.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit API interface for authentication endpoints.
 */
interface AuthApi {
    /**
     * Authenticate user with email and password.
     * Note: json-server doesn't support POST body matching, so we use query params
     * @param email User's email
     * @param password User's password
     * @return User data with authentication token
     */
    @GET("/api/v1/auth/login")
    suspend fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): Response<List<UserDto>>
    
    /**
     * Alternative login endpoint using POST body (for future real API)
     * @param loginRequest Login credentials
     * @return User data with authentication token
     */
    @POST("/api/v1/auth/login")
    suspend fun loginWithBody(
        @Body loginRequest: LoginRequest
    ): Response<UserDto>
}
