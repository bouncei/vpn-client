package com.vpnclient.app.domain.usecase

import com.vpnclient.app.domain.model.VpnNode
import com.vpnclient.app.domain.repository.NodeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching and observing VPN nodes.
 * Handles business logic for node retrieval and sorting.
 */
class GetNodesUseCase @Inject constructor(
    private val nodeRepository: NodeRepository
) {
    /**
     * Get all available VPN nodes, sorted by latency.
     * @param forceRefresh Whether to force refresh from network
     * @return Result containing sorted list of VPN nodes
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<VpnNode>> {
        return nodeRepository.getNodes(forceRefresh).map { nodes ->
            // Sort nodes by latency for better user experience
            nodes.sortedBy { it.latencyMs }
        }
    }
    
    /**
     * Observe cached VPN nodes with real-time updates.
     * @return Flow emitting sorted list of nodes
     */
    fun observeNodes(): Flow<List<VpnNode>> {
        return nodeRepository.observeNodes()
    }
    
    /**
     * Get nodes filtered by region/country.
     * @param country Country to filter by (null for all countries)
     * @param forceRefresh Whether to force refresh from network
     * @return Result containing filtered and sorted nodes
     */
    suspend fun getNodesByCountry(
        country: String? = null,
        forceRefresh: Boolean = false
    ): Result<List<VpnNode>> {
        return invoke(forceRefresh).map { nodes ->
            if (country != null) {
                nodes.filter { it.country.equals(country, ignoreCase = true) }
            } else {
                nodes
            }
        }
    }
}
