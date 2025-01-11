private val desktopClientConfig = object : ClientConfig {
    override fun isDebugEnabled() = System.getenv().containsKey("DEBUG_MODE")

    override var backendHost: String = "http://localhost:8080"
}

actual fun getClientConfig() = desktopClientConfig