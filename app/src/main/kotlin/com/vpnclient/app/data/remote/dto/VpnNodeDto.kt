package com.vpnclient.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.vpnclient.app.domain.model.VpnNode

/**
 * Data Transfer Object for VPN Node API responses.
 * Maps JSON response to domain model.
 */
data class VpnNodeDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("latency_ms")
    val latencyMs: Int,
    @SerializedName("public_key")
    val publicKey: String,
    @SerializedName("endpoint_ip")
    val endpointIp: String
) {
    /**
     * Convert DTO to domain model.
     * @return VpnNode domain object
     */
    fun toDomain(): VpnNode {
        return VpnNode(
            id = id,
            name = name,
            country = country,
            latencyMs = latencyMs,
            publicKey = publicKey,
            endpointIp = endpointIp
        )
    }
}
