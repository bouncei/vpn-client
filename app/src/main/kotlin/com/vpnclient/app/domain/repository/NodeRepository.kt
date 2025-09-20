package com.vpnclient.app.domain.repository

import com.vpnclient.app.domain.model.VpnNode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for VPN node operations.
 * Handles fetching and caching of available VPN servers.
 */
interface NodeRepository {
    /**
     * Get all available VPN nodes.
     * @param forceRefresh Whether to force refresh from network (bypass cache)
     * @return Result containing list of VPN nodes on success, or error on failure
     */
    suspend fun getNodes(forceRefresh: Boolean = false): Result<List<VpnNode>>

    /**
     * Get a specific VPN node by ID.
     * @param nodeId Unique identifier of the node
     * @return Result containing VPN node on success, or error on failure
     */
    suspend fun getNodeById(nodeId: String): Result<VpnNode?>

    /**
     * Observe cached VPN nodes.
     * @return Flow emitting the current list of cached nodes
     */
    fun observeNodes(): Flow<List<VpnNode>>

    /**
     * Clear cached node data.
     */
    suspend fun clearCache()
}
