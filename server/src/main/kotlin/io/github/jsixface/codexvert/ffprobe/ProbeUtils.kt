package io.github.jsixface.codexvert.ffprobe

import io.github.jsixface.codexvert.logger
import kotlinx.serialization.json.Json
import java.nio.file.Path

object ProbeUtils {
    private val logger = logger()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parseMediaInfo(path: Path): ProbeInfo? {
        logger.info("Parsing file $path")
        val builder =
            ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-of", "json",
                "-pretty",
                "-show_entries", "stream:program:format:chapter",
                path.toAbsolutePath().toString(),
            )
        var output = ""
        return try {
            val process = builder.start()
            output = process.inputStream.use { it.bufferedReader().readText() }
            process.waitFor()
            val probeInfo = json.decodeFromString<ProbeInfo>(output)
            logger.debug("Probe info: $probeInfo")
            probeInfo
        } catch (e: Exception) {
            logger.error("Cant get file information for $path. Output = $output", e)
            null
        }
    }
}
