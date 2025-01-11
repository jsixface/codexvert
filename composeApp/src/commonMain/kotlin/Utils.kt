expect fun getClientConfig(): ClientConfig

interface ClientConfig {
    fun isDebugEnabled(): Boolean

    var backendHost: String
}