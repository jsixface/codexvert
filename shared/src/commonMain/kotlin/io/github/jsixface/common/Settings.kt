package io.github.jsixface.common

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Settings(
    val libraryLocations: List<String> = listOf(),
    val workspaceLocation: String = "/tmp/vid-con",
    val videoExtensions: List<String> = listOf("avi", "mp4", "mkv", "mpeg4"),
    val watchDuration: Duration? = null,
)