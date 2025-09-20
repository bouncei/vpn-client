package com.vpnclient.app.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vpnclient.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for VPN-related notifications.
 * Handles connection status notifications and system integration.
 */
@Singleton
class VpnNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "vpn_status_channel"
        private const val CHANNEL_NAME = "VPN Status"
        private const val CHANNEL_DESCRIPTION = "Notifications for VPN connection status"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    /**
     * Create notification channel for Android 8.0+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification for successful VPN connection.
     * @param nodeName Name of the connected VPN node
     */
    fun showConnectedNotification(nodeName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn_connected) // You'll need to add this icon
            .setContentTitle(context.getString(R.string.connection_notification_title))
            .setContentText(context.getString(R.string.connected_to_node, nodeName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false)
            .setOngoing(true) // Keep notification while connected
            .setColor(context.getColor(android.R.color.holo_green_dark))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            // In a real app, you might want to request permission
        }
    }

    /**
     * Show notification for VPN disconnection.
     */
    fun showDisconnectedNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn_disconnected) // You'll need to add this icon
            .setContentTitle(context.getString(R.string.connection_notification_title))
            .setContentText(context.getString(R.string.disconnected_from_vpn))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(context.getColor(android.R.color.holo_red_dark))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    /**
     * Show notification for connection error.
     * @param errorMessage Error message to display
     */
    fun showConnectionErrorNotification(errorMessage: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn_error) // You'll need to add this icon
            .setContentTitle("VPN Connection Failed")
            .setContentText(errorMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(context.getColor(android.R.color.holo_orange_dark))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    /**
     * Clear all VPN notifications.
     */
    fun clearNotifications() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    /**
     * Check if notification permission is granted (Android 13+).
     * @return True if permission is granted or not required
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true // Permission not required for older versions
        }
    }
}
