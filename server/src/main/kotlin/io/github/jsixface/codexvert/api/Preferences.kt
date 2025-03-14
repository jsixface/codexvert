package io.github.jsixface.codexvert.api

import io.github.jsixface.common.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class SavedData(val settings: Settings)

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}
private const val DATA_FILE_NAME = ".codexvert.json"
private val homeDir = System.getenv()["HOME"] ?: "."
private val dataFile = File(homeDir, DATA_FILE_NAME)

interface IPreferences {
    fun getSettings(): Settings
    fun saveSettings(settings: Settings)
}

class Preferences : IPreferences {
    private var settings: Settings = load().settings


    private fun load(): SavedData {
        if (dataFile.exists().not()) dataFile.createNewFile()
        val dataStr = dataFile.readText()
        return if (dataStr.isNotBlank())
            json.decodeFromString(dataStr)
        else
            SavedData(Settings())
    }

    override fun getSettings(): Settings = settings

    override fun saveSettings(settings: Settings) {
        this.settings = settings
        val dataStr = json.encodeToString(SavedData(settings))
        dataFile.writeText(dataStr)
    }
}