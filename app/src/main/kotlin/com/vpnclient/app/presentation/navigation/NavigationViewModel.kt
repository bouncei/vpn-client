package com.vpnclient.app.presentation.navigation

import androidx.lifecycle.ViewModel
import com.vpnclient.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for navigation logic.
 * Provides access to authentication state for routing decisions.
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()
