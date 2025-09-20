package com.vpnclient.app.domain.usecase

import com.vpnclient.app.domain.repository.ConnectionRepository
import com.vpnclient.app.domain.repository.NodeRepository
import javax.inject.Inject

/**
 * Use case for connecting to a VPN node.
 * Validates node availability and initiates connection.
 */
class ConnectToNodeUseCase @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val nodeRepository: NodeRepository
) {
    /**
     * Connect to a specific VPN node with strategy selection.
     * @param nodeId ID of the node to connect to
     * @param strategy Connection strategy ("fast" or "secure")
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        nodeId: String,
        strategy: String = "fast"
    ): Result<Unit> {
        // Validate node exists
        val nodeResult = nodeRepository.getNodeById(nodeId)
        if (nodeResult.isFailure) {
            return Result.failure(
                IllegalArgumentException("Node not found: $nodeId")
            )
        }
        
        val node = nodeResult.getOrNull()
        if (node == null) {
            return Result.failure(
                IllegalArgumentException("Node not available: $nodeId")
            )
        }
        
        // Validate strategy
        if (strategy !in listOf("fast", "secure")) {
            return Result.failure(
                IllegalArgumentException("Invalid strategy: $strategy. Must be 'fast' or 'secure'")
            )
        }
        
        // Delegate to connection repository
        return connectionRepository.connect(nodeId, strategy)
    }
}
