package io.github.jsixface.codexvert.utils

import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.MediaProbeInfo
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.TrackType
import kotlinx.serialization.json.Json

object CodecUtils {
    private val logger = logger()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseMediaInfo(path: String): List<MediaTrack>? {
        logger.info("Parsing file $path")
        val builder =
                ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "stream", "-pretty", "-of", "json", path)
        val probeInfo = runCatching {
            val process = builder.start()
            val output = process.inputStream.use { it.bufferedReader().readText() }
            process.waitFor()
            json.decodeFromString<MediaProbeInfo>(output)
        }
        probeInfo.exceptionOrNull()?.let { logger.error("Cant get ") }
        return probeInfo.getOrNull()?.streams?.mapNotNull { s ->
            val trackType = when (s.codecType) {
                "audio" -> TrackType.Audio
                "video" -> TrackType.Video
                "subtitle" -> TrackType.Subtitle
                else -> null
            }
            trackType?.let { MediaTrack(trackType, s.index, s.codecName) }
        }
    }
}
