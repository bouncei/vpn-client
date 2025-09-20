package com.vpnclient.app.data.remote.api

import com.vpnclient.app.data.remote.dto.VpnNodeDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API interface for VPN node endpoints.
 */
interface NodeApi {
    /**
     * Get all available VPN nodes.
     * @return List of VPN nodes
     */
    @GET("/api/v1/nodes")
    suspend fun getNodes(): Response<List<VpnNodeDto>>
    
    /**
     * Get a specific VPN node by ID.
     * @param nodeId Unique identifier of the node
     * @return VPN node data
     */
    @GET("/api/v1/nodes/{nodeId}")
    suspend fun getNodeById(@Path("nodeId") nodeId: String): Response<VpnNodeDto>
}
