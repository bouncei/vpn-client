package com.vpnclient.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vpnclient.app.domain.repository.AuthRepository
import com.vpnclient.app.presentation.auth.SignInScreen
import com.vpnclient.app.presentation.nodes.NodeListScreen

/**
 * Main navigation composable for the VPN client app.
 * Handles routing between authentication and main app screens.
 */
@Composable
fun VpnNavigation(
    authRepository: AuthRepository = hiltViewModel<NavigationViewModel>().authRepository
) {
    val navController = rememberNavController()
    val isAuthenticated by authRepository.isAuthenticated().collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) {
            VpnDestination.NodeList.route
        } else {
            VpnDestination.SignIn.route
        }
    ) {
        composable(VpnDestination.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(VpnDestination.NodeList.route) {
                        popUpTo(VpnDestination.SignIn.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(VpnDestination.NodeList.route) {
            NodeListScreen(
                onLogout = {
                    navController.navigate(VpnDestination.SignIn.route) {
                        popUpTo(VpnDestination.NodeList.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Sealed class defining navigation destinations.
 */
sealed class VpnDestination(val route: String) {
    object SignIn : VpnDestination("sign_in")
    object NodeList : VpnDestination("node_list")
}
