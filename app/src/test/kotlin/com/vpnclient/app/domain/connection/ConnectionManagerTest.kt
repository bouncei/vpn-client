package com.vpnclient.app.domain.connection

import com.vpnclient.app.domain.model.ConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConnectionManager.
 * Tests basic functionality without complex timing dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionManagerTest {

    private lateinit var connectionManager: ConnectionManager

    @Before
    fun setup() {
        connectionManager = ConnectionManager()
    }

    @Test
    fun `initial state is disconnected`() = runTest {
        // Given - ConnectionManager is initialized

        // When
        val state = connectionManager.connectionState.first()

        // Then
        assertEquals(ConnectionState.Disconnected, state)
    }

    @Test
    fun `connect with fast strategy succeeds`() = runTest {
        // Given
        val nodeId = "us-east-1"
        val strategy = "fast"

        // When
        val result = connectionManager.connect(nodeId, strategy)

        // Then
        assertTrue("Connect should return success", result.isSuccess)
    }

    @Test
    fun `connect with secure strategy succeeds`() = runTest {
        // Given
        val nodeId = "eu-west-1"
        val strategy = "secure"

        // When
        val result = connectionManager.connect(nodeId, strategy)

        // Then
        assertTrue("Connect should return success", result.isSuccess)
    }

    @Test
    fun `getCurrentState returns current connection state`() = runTest {
        // Given - Initial state
        val initialState = connectionManager.getCurrentState()
        assertEquals("Initial state should be Disconnected", ConnectionState.Disconnected, initialState)

        // When - Start connecting
        connectionManager.connect("us-east-1", "fast")
        val connectingState = connectionManager.getCurrentState()

        // Then - Should be in some connection-related state
        assertTrue("Should be in connecting or connected state", 
                  connectingState is ConnectionState.Connecting || 
                  connectingState is ConnectionState.Connected)
    }

    @Test
    fun `isConnectedToNode initially returns false`() = runTest {
        // Given
        val nodeId = "us-east-1"
        
        // When
        val isConnected = connectionManager.isConnectedToNode(nodeId)
        
        // Then
        assertTrue("Should not be connected initially", !isConnected)
    }

    @Test
    fun `isConnectingToNode initially returns false`() = runTest {
        // Given
        val nodeId = "us-east-1"
        
        // When
        val isConnecting = connectionManager.isConnectingToNode(nodeId)
        
        // Then
        assertTrue("Should not be connecting initially", !isConnecting)
    }
}
