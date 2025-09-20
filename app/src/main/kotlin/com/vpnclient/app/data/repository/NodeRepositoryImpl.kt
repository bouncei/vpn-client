package com.vpnclient.app.data.repository

import com.vpnclient.app.data.remote.api.NodeApi
import com.vpnclient.app.domain.model.VpnNode
import com.vpnclient.app.domain.repository.NodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NodeRepository using Retrofit API with in-memory caching.
 * Handles VPN node data fetching and caching logic.
 */
@Singleton
class NodeRepositoryImpl @Inject constructor(
    private val nodeApi: NodeApi
) : NodeRepository {

    private val _cachedNodes = MutableStateFlow<List<VpnNode>>(emptyList())
    private var lastFetchTime = 0L
    private val cacheValidityMs = 5 * 60 * 1000L // 5 minutes

    override suspend fun getNodes(forceRefresh: Boolean): Result<List<VpnNode>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val isCacheValid = (currentTime - lastFetchTime) < cacheValidityMs
            
            if (!forceRefresh && isCacheValid && _cachedNodes.value.isNotEmpty()) {
                // Return cached data
                Result.success(_cachedNodes.value)
            } else {
                // Fetch from network
                val response = nodeApi.getNodes()
                
                if (response.isSuccessful) {
                    val nodes = response.body()?.map { it.toDomain() } ?: emptyList()
                    
                    // Update cache
                    _cachedNodes.value = nodes
                    lastFetchTime = currentTime
                    
                    Result.success(nodes)
                } else {
                    Result.failure(Exception("Failed to fetch nodes: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            // Return cached data if available, otherwise return error
            if (_cachedNodes.value.isNotEmpty()) {
                Result.success(_cachedNodes.value)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getNodeById(nodeId: String): Result<VpnNode?> {
        return try {
            // First check cache
            val cachedNode = _cachedNodes.value.find { it.id == nodeId }
            if (cachedNode != null) {
                return Result.success(cachedNode)
            }
            
            // If not in cache, try API
            val response = nodeApi.getNodeById(nodeId)
            
            if (response.isSuccessful) {
                val node = response.body()?.toDomain()
                Result.success(node)
            } else {
                Result.failure(Exception("Node not found: $nodeId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeNodes(): Flow<List<VpnNode>> {
        return _cachedNodes.asStateFlow()
    }

    override suspend fun clearCache() {
        _cachedNodes.value = emptyList()
        lastFetchTime = 0L
    }
}
