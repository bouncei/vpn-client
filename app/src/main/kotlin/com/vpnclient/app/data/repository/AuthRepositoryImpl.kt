package com.vpnclient.app.data.repository

import com.vpnclient.app.data.local.datastore.AuthDataStore
import com.vpnclient.app.data.remote.api.AuthApi
import com.vpnclient.app.domain.model.User
import com.vpnclient.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository using Retrofit API and DataStore.
 * Handles authentication logic and credential persistence.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val authDataStore: AuthDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Call API to authenticate
            val response = authApi.login(email, password)
            
            if (response.isSuccessful) {
                val users = response.body()
                // json-server returns array, find matching user
                val user = users?.find { 
                    it.email == email && it.password == password 
                }?.toDomain()
                
                if (user != null) {
                    // Save user data locally
                    authDataStore.saveUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid credentials"))
                }
            } else {
                Result.failure(Exception("Authentication failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        authDataStore.clearAuth()
    }

    override suspend fun getStoredToken(): String? {
        return authDataStore.getToken().first()
    }

    override fun getCurrentUser(): Flow<User?> {
        return authDataStore.getUser()
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return authDataStore.isAuthenticated()
    }
}
