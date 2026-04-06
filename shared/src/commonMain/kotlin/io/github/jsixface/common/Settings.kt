package io.github.jsixface.common

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val libraryLocations: List<String> = listOf(),
    val workspaceLocation: String = "/tmp/vid-con",
    val videoExtensions: List<String> = listOf("avi", "mp4", "mkv", "mpeg4"),
    val autoConversion: AutoConversion = AutoConversion(),
    val takeBackups: Boolean = true,
)

@Serializable
data class AutoConversion(
    val conversion: Map<Codec, Codec> = mapOf(Codec.AAC to Codec.AAC),
)