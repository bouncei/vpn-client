package com.vpnclient.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vpnclient.app.domain.model.ConnectionSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore wrapper for VPN session-related preferences.
 * Handles persistence of connection state and session data.
 */
@Singleton
class SessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val SESSION_ID_KEY = longPreferencesKey("session_id")
        private val USER_ID_KEY = longPreferencesKey("session_user_id")
        private val NODE_ID_KEY = stringPreferencesKey("session_node_id")
        private val CONNECTED_AT_KEY = longPreferencesKey("connected_at")
        private val IS_ACTIVE_KEY = booleanPreferencesKey("session_is_active")
    }
    
    /**
     * Save current connection session.
     * @param session Session data to persist
     */
    suspend fun saveSession(session: ConnectionSession) {
        dataStore.edit { preferences ->
            preferences[SESSION_ID_KEY] = session.id
            preferences[USER_ID_KEY] = session.userId
            preferences[NODE_ID_KEY] = session.nodeId
            preferences[CONNECTED_AT_KEY] = session.connectedAt
            preferences[IS_ACTIVE_KEY] = session.isActive
        }
    }
    
    /**
     * Get current active session.
     * @return Flow emitting current session or null
     */
    fun getCurrentSession(): Flow<ConnectionSession?> {
        return dataStore.data.map { preferences ->
            val sessionId = preferences[SESSION_ID_KEY]
            val userId = preferences[USER_ID_KEY]
            val nodeId = preferences[NODE_ID_KEY]
            val connectedAt = preferences[CONNECTED_AT_KEY]
            val isActive = preferences[IS_ACTIVE_KEY] ?: false
            
            if (sessionId != null && userId != null && nodeId != null && connectedAt != null && isActive) {
                ConnectionSession(
                    id = sessionId,
                    userId = userId,
                    nodeId = nodeId,
                    connectedAt = connectedAt,
                    isActive = isActive
                )
            } else {
                null
            }
        }
    }
    
    /**
     * Mark current session as disconnected.
     */
    suspend fun disconnectSession() {
        dataStore.edit { preferences ->
            preferences[IS_ACTIVE_KEY] = false
        }
    }
    
    /**
     * Clear all session data.
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(SESSION_ID_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(NODE_ID_KEY)
            preferences.remove(CONNECTED_AT_KEY)
            preferences.remove(IS_ACTIVE_KEY)
        }
    }
}
