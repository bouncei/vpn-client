package com.vpnclient.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for VPN Client.
 * Enables Hilt dependency injection throughout the app.
 */
@HiltAndroidApp
class VpnClientApplication : Application()
