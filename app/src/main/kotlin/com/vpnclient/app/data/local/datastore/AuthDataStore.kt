package com.vpnclient.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vpnclient.app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore wrapper for authentication-related preferences.
 * Handles secure storage of user credentials and tokens.
 */
@Singleton
class AuthDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
    
    /**
     * Save user authentication data.
     * @param user User object containing id, email, and token
     */
    suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
            preferences[AUTH_TOKEN_KEY] = user.token
        }
    }
    
    /**
     * Get stored authentication token.
     * @return Flow emitting current token or null
     */
    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    }
    
    /**
     * Get stored user data.
     * @return Flow emitting current user or null
     */
    fun getUser(): Flow<User?> {
        return dataStore.data.map { preferences ->
            val id = preferences[USER_ID_KEY]
            val email = preferences[USER_EMAIL_KEY]
            val token = preferences[AUTH_TOKEN_KEY]
            
            if (id != null && email != null && token != null) {
                User(id = id, email = email, token = token)
            } else {
                null
            }
        }
    }
    
    /**
     * Check if user is authenticated.
     * @return Flow emitting authentication status
     */
    fun isAuthenticated(): Flow<Boolean> {
        return getToken().map { token ->
            !token.isNullOrBlank()
        }
    }
    
    /**
     * Clear all stored authentication data.
     */
    suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }
}
