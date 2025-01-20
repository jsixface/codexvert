package io.github.jsixface.codexvert.api

import io.github.jsixface.common.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class SavedData(val settings: Settings) {

    fun save() {
        val dataStr = json.encodeToString(this)
        synchronized(lock) {
            dataFile.writeText(dataStr)
        }
    }

    companion object {
        private val lock = Any()

        fun load(): SavedData {
            if (dataFile.exists().not()) dataFile.createNewFile()
            val dataStr = synchronized(lock) { dataFile.readText() }
            return if (dataStr.isNotBlank())
                json.decodeFromString(dataStr)
            else
                SavedData(Settings())
        }
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}
private const val DATA_FILE_NAME = ".codexvert.json"
private val homeDir = System.getenv()["HOME"] ?: "."
private val dataFile = File(homeDir, DATA_FILE_NAME)

