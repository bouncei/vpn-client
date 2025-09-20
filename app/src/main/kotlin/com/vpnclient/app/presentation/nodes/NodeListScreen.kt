package com.vpnclient.app.presentation.nodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
// import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
// import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
// import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vpnclient.app.R
import com.vpnclient.app.domain.model.ConnectionState
import com.vpnclient.app.domain.model.VpnNode

/**
 * Node list screen showing available VPN servers.
 * Supports pull-to-refresh and connection management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeListScreen(
    viewModel: NodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // val pullToRefreshState = rememberPullToRefreshState()

    // Handle pull-to-refresh
    // LaunchedEffect(pullToRefreshState.isRefreshing) {
    //     if (pullToRefreshState.isRefreshing) {
    //         viewModel.refreshNodes()
    //     }
    // }

    // LaunchedEffect(uiState.isLoading) {
    //     if (!uiState.isLoading) {
    //         pullToRefreshState.endRefresh()
    //     }
    // }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.nodes),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshNodes() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh nodes"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // .nestedScroll(pullToRefreshState.nestedScrollConnection)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.nodes.isEmpty() -> {
                    // Initial loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.loading),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                uiState.nodes.isEmpty() && !uiState.isLoading -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh, // Using Refresh icon instead of VpnKey
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No VPN nodes available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.refreshNodes() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                else -> {
                    // Node list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Connection status header
                        item {
                            ConnectionStatusCard(
                                connectionState = uiState.connectionState,
                                onDisconnect = viewModel::disconnect
                            )
                        }

                        // Node items
                        items(
                            items = uiState.nodes,
                            key = { node -> node.id }
                        ) { node ->
                            NodeItem(
                                node = node,
                                isConnected = uiState.isConnectedToNode(node.id),
                                isConnecting = uiState.isConnectingToNode(node.id),
                                canConnect = uiState.canConnect,
                                onConnect = { strategy ->
                                    viewModel.connectToNode(node.id, strategy)
                                }
                            )
                        }
                    }
                }
            }

            // Pull to refresh indicator
            // PullToRefreshContainer(
            //     state = pullToRefreshState,
            //     modifier = Modifier.align(Alignment.TopCenter)
            // )
        }
    }
}

/**
 * Connection status card showing current VPN state.
 */
@Composable
private fun ConnectionStatusCard(
    connectionState: ConnectionState,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is ConnectionState.Connected -> MaterialTheme.colorScheme.primaryContainer
                is ConnectionState.Connecting -> MaterialTheme.colorScheme.secondaryContainer
                is ConnectionState.Error -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = when (connectionState) {
                    is ConnectionState.Connected -> "Connected to ${connectionState.nodeId}"
                    is ConnectionState.Connecting -> "Connecting to ${connectionState.nodeId}..."
                    is ConnectionState.Disconnecting -> "Disconnecting..."
                    is ConnectionState.Error -> "Connection Error"
                    ConnectionState.Disconnected -> "Not Connected"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            when (connectionState) {
                is ConnectionState.Connecting -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = connectionState.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                is ConnectionState.Connected -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.disconnect))
                    }
                }
                
                is ConnectionState.Error -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = connectionState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                else -> { /* No additional content */ }
            }
        }
    }
}

/**
 * Individual node item in the list.
 */
@Composable
private fun NodeItem(
    node: VpnNode,
    isConnected: Boolean,
    isConnecting: Boolean,
    canConnect: Boolean,
    onConnect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = node.country,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.latency_ms, node.latencyMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            node.latencyMs < 50 -> Color(0xFF4CAF50) // Green
                            node.latencyMs < 100 -> Color(0xFFFF9800) // Orange
                            else -> Color(0xFFF44336) // Red
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    isConnected -> {
                        Button(
                            onClick = { /* Already connected */ },
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.connected))
                        }
                    }
                    
                    isConnecting -> {
                        Button(
                            onClick = { /* Connecting */ },
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.connecting))
                        }
                    }
                    
                    else -> {
                        Button(
                            onClick = { onConnect("fast") },
                            enabled = canConnect,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fast Connect")
                        }
                        
                        OutlinedButton(
                            onClick = { onConnect("secure") },
                            enabled = canConnect,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Secure")
                        }
                    }
                }
            }
        }
    }
}
