import kotlinx.browser.window

private val wasmClientConfig = object : ClientConfig {
    override fun isDebugEnabled() = window.location.hash.lowercase().contains("debug")

    override var backendHost: String
        get() = ""
        set(value) {}
}

actual fun getClientConfig() = wasmClientConfig