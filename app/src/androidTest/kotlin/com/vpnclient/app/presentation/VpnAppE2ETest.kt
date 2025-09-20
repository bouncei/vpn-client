package com.vpnclient.app.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI test for the VPN client app.
 * Tests the complete user flow from sign-in to node connection.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class VpnAppE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun completeUserFlow_signInToNodeConnection() {
        // Start at sign-in screen
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        
        // Initially, sign-in button should be disabled (empty form)
        composeTestRule.onNodeWithText("Sign In").assertIsNotEnabled()
        
        // Fill in email
        composeTestRule.onNodeWithText("Email").performTextInput("test@vpn.com")
        
        // Fill in password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        
        // Now sign-in button should be enabled
        composeTestRule.onNodeWithText("Sign In").assertIsEnabled()
        
        // Click sign-in button
        composeTestRule.onNodeWithText("Sign In").performClick()
        
        // Should navigate to nodes screen (wait for authentication)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("VPN Nodes").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify we're on the nodes screen
        composeTestRule.onNodeWithText("VPN Nodes").assertIsDisplayed()
        
        // Should see connection status card
        composeTestRule.onNodeWithText("Not Connected").assertIsDisplayed()
        
        // Should see node list (wait for nodes to load)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("New York").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify nodes are displayed
        composeTestRule.onNodeWithText("New York").assertIsDisplayed()
        composeTestRule.onNodeWithText("United States").assertIsDisplayed()
        composeTestRule.onNodeWithText("London").assertIsDisplayed()
        composeTestRule.onNodeWithText("United Kingdom").assertIsDisplayed()
        
        // Find and click "Fast Connect" button for New York node
        composeTestRule.onNodeWithText("Fast Connect").performClick()
        
        // Should show connecting state
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                composeTestRule.onNodeWithText("Connecting...").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Wait for connection to complete
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithText("Connected to us-east-1").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify connected state
        composeTestRule.onNodeWithText("Connected to us-east-1").assertIsDisplayed()
        
        // Should see disconnect button
        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
        
        // The connected node should show "Connected" button (disabled)
        composeTestRule.onNodeWithText("Connected").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connected").assertIsNotEnabled()
    }

    @Test
    fun signInWithInvalidCredentials_showsError() {
        // Start at sign-in screen
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        
        // Fill in invalid credentials
        composeTestRule.onNodeWithText("Email").performTextInput("invalid@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")
        
        // Click sign-in button
        composeTestRule.onNodeWithText("Sign In").performClick()
        
        // Should show error message (wait for API call)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Invalid credentials").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Should still be on sign-in screen
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun refreshNodes_updatesNodeList() {
        // First sign in
        signInSuccessfully()
        
        // Should be on nodes screen
        composeTestRule.onNodeWithText("VPN Nodes").assertIsDisplayed()
        
        // Click refresh button
        composeTestRule.onNodeWithContentDescription("Refresh nodes").performClick()
        
        // Should show loading state briefly, then nodes again
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("New York").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Verify nodes are still displayed after refresh
        composeTestRule.onNodeWithText("New York").assertIsDisplayed()
    }

    /**
     * Helper function to sign in successfully.
     */
    private fun signInSuccessfully() {
        composeTestRule.onNodeWithText("Email").performTextInput("test@vpn.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Sign In").performClick()
        
        // Wait for navigation to nodes screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("VPN Nodes").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
