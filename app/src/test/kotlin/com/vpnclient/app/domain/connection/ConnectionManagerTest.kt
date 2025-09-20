package com.vpnclient.app.domain.connection

import com.vpnclient.app.domain.model.ConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConnectionManager.
 * Tests connection state management and strategy patterns.
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
        assertTrue(result.isSuccess)
    }

    @Test
    fun `connect transitions through connecting to connected state`() = runTest {
        // Given
        val nodeId = "us-east-1"
        val strategy = "fast"

        // When
        connectionManager.connect(nodeId, strategy)
        
        // Check connecting state
        advanceTimeBy(100L)
        val connectingState = connectionManager.getCurrentState()
        assertTrue(connectingState is ConnectionState.Connecting)
        assertEquals(nodeId, (connectingState as ConnectionState.Connecting).nodeId)

        // Wait for connection to complete
        advanceTimeBy(2000L) // Fast strategy takes ~1.5s total
        val connectedState = connectionManager.getCurrentState()
        assertTrue(connectedState is ConnectionState.Connected)
        assertEquals(nodeId, (connectedState as ConnectionState.Connected).nodeId)
    }

    @Test
    fun `connect with secure strategy takes longer`() = runTest {
        // Given
        val nodeId = "eu-west-1"
        val strategy = "secure"

        // When
        connectionManager.connect(nodeId, strategy)
        
        // After fast strategy time, should still be connecting
        advanceTimeBy(2000L)
        val stillConnecting = connectionManager.getCurrentState()
        assertTrue(stillConnecting is ConnectionState.Connecting)

        // After secure strategy time, should be connected
        advanceTimeBy(4000L) // Secure strategy takes ~5s total
        val connectedState = connectionManager.getCurrentState()
        assertTrue(connectedState is ConnectionState.Connected)
    }

    @Test
    fun `disconnect from connected state succeeds`() = runTest {
        // Given - First connect
        val nodeId = "us-east-1"
        connectionManager.connect(nodeId, "fast")
        advanceTimeBy(2000L) // Wait for connection

        // When
        val result = connectionManager.disconnect()

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `disconnect transitions through disconnecting to disconnected state`() = runTest {
        // Given - First connect
        val nodeId = "us-east-1"
        connectionManager.connect(nodeId, "fast")
        advanceTimeBy(2000L) // Wait for connection

        // When
        connectionManager.disconnect()
        
        // Check disconnecting state
        advanceTimeBy(100L)
        val disconnectingState = connectionManager.getCurrentState()
        assertTrue(disconnectingState is ConnectionState.Disconnecting)

        // Wait for disconnection to complete
        advanceTimeBy(1500L)
        val disconnectedState = connectionManager.getCurrentState()
        assertEquals(ConnectionState.Disconnected, disconnectedState)
    }

    @Test
    fun `cannot connect when already connected`() = runTest {
        // Given - First connect
        val nodeId1 = "us-east-1"
        connectionManager.connect(nodeId1, "fast")
        advanceTimeBy(2000L) // Wait for connection

        // When - Try to connect to another node
        val nodeId2 = "eu-west-1"
        val result = connectionManager.connect(nodeId2, "fast")

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `isConnectedToNode returns correct status`() = runTest {
        // Given
        val nodeId = "us-east-1"
        
        // Initially not connected
        assertTrue(!connectionManager.isConnectedToNode(nodeId))

        // Connect and wait
        connectionManager.connect(nodeId, "fast")
        advanceTimeBy(2000L)

        // Now should be connected
        assertTrue(connectionManager.isConnectedToNode(nodeId))
        
        // Should not be connected to different node
        assertTrue(!connectionManager.isConnectedToNode("eu-west-1"))
    }

    @Test
    fun `isConnectingToNode returns correct status`() = runTest {
        // Given
        val nodeId = "us-east-1"
        
        // Initially not connecting
        assertTrue(!connectionManager.isConnectingToNode(nodeId))

        // Start connecting
        connectionManager.connect(nodeId, "fast")
        advanceTimeBy(500L) // During connection

        // Now should be connecting
        assertTrue(connectionManager.isConnectingToNode(nodeId))
        
        // Wait for completion
        advanceTimeBy(2000L)
        
        // Should no longer be connecting
        assertTrue(!connectionManager.isConnectingToNode(nodeId))
    }
}
