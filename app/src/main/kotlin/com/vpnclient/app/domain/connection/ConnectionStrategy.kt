package com.vpnclient.app.domain.connection

/**
 * Strategy interface for different VPN connection approaches.
 * Implements the Strategy pattern for connection behavior.
 */
interface ConnectionStrategy {
    /**
     * Get the connection steps for this strategy.
     * @return List of connection step descriptions
     */
    fun getConnectionSteps(): List<String>

    /**
     * Get the delay between connection steps in milliseconds.
     * @return Delay in milliseconds
     */
    fun getStepDelay(): Long

    /**
     * Get the total estimated connection time.
     * @return Total time in milliseconds
     */
    fun getTotalConnectionTime(): Long = getConnectionSteps().size * getStepDelay()

    /**
     * Get the strategy name for display purposes.
     * @return Human-readable strategy name
     */
    fun getStrategyName(): String
}

/**
 * Fast connection strategy with minimal security checks.
 * Optimized for speed over security.
 */
class FastConnectionStrategy : ConnectionStrategy {
    override fun getConnectionSteps(): List<String> = listOf(
        "Establishing connection...",
        "Authenticating...",
        "Connected!"
    )

    override fun getStepDelay(): Long = 500L

    override fun getStrategyName(): String = "Fast"
}

/**
 * Secure connection strategy with comprehensive security checks.
 * Prioritizes security over connection speed.
 */
class SecureConnectionStrategy : ConnectionStrategy {
    override fun getConnectionSteps(): List<String> = listOf(
        "Initializing secure handshake...",
        "Verifying server certificate...",
        "Establishing encrypted tunnel...",
        "Performing security validation...",
        "Connection secured!"
    )

    override fun getStepDelay(): Long = 1000L

    override fun getStrategyName(): String = "Secure"
}

/**
 * Factory for creating connection strategies.
 */
object ConnectionStrategyFactory {
    /**
     * Create a connection strategy by name.
     * @param strategyName Name of the strategy ("fast" or "secure")
     * @return ConnectionStrategy instance
     */
    fun createStrategy(strategyName: String): ConnectionStrategy {
        return when (strategyName.lowercase()) {
            "fast" -> FastConnectionStrategy()
            "secure" -> SecureConnectionStrategy()
            else -> FastConnectionStrategy() // Default to fast
        }
    }

    /**
     * Get all available strategy names.
     * @return List of strategy names
     */
    fun getAvailableStrategies(): List<String> = listOf("fast", "secure")
}
