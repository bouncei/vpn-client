package com.vpnclient.app.presentation.notification

import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.vpnclient.app.domain.model.ConnectionState
import com.vpnclient.app.domain.repository.ConnectionRepository
import com.vpnclient.app.domain.repository.NodeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background service for monitoring VPN connection state and showing notifications.
 * Uses LifecycleService to properly handle coroutines and lifecycle.
 */
@AndroidEntryPoint
class ConnectionNotificationService : LifecycleService() {

    @Inject
    lateinit var connectionRepository: ConnectionRepository

    @Inject
    lateinit var nodeRepository: NodeRepository

    @Inject
    lateinit var notificationManager: VpnNotificationManager

    private var lastConnectionState: ConnectionState = ConnectionState.Disconnected

    override fun onCreate() {
        super.onCreate()
        startObservingConnectionState()
    }

    /**
     * Observe connection state changes and show appropriate notifications.
     */
    private fun startObservingConnectionState() {
        lifecycleScope.launch {
            connectionRepository.observeConnectionState().collect { connectionState ->
                handleConnectionStateChange(connectionState)
            }
        }
    }

    /**
     * Handle connection state changes and trigger notifications.
     * @param newState New connection state
     */
    private suspend fun handleConnectionStateChange(newState: ConnectionState) {
        // Only show notifications for significant state changes
        when {
            // Connected notification
            newState is ConnectionState.Connected && 
            lastConnectionState !is ConnectionState.Connected -> {
                showConnectedNotification(newState.nodeId)
            }

            // Disconnected notification (only if was previously connected)
            newState is ConnectionState.Disconnected && 
            lastConnectionState is ConnectionState.Connected -> {
                notificationManager.showDisconnectedNotification()
            }

            // Error notification
            newState is ConnectionState.Error && 
            lastConnectionState !is ConnectionState.Error -> {
                notificationManager.showConnectionErrorNotification(newState.error)
            }
        }

        lastConnectionState = newState
    }

    /**
     * Show connected notification with node name.
     * @param nodeId ID of the connected node
     */
    private suspend fun showConnectedNotification(nodeId: String) {
        try {
            val nodeResult = nodeRepository.getNodeById(nodeId)
            val nodeName = nodeResult.getOrNull()?.name ?: nodeId
            notificationManager.showConnectedNotification(nodeName)
        } catch (e: Exception) {
            // Fallback to node ID if can't get node name
            notificationManager.showConnectedNotification(nodeId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear notifications when service is destroyed
        notificationManager.clearNotifications()
    }
}
