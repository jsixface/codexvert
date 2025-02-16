package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.Codec
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.VideoFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class Watchers(
    private val videoApi: VideoApi,
    private val repo: IVideoFilesRepo,
    private val conversionApi: ConversionApi
) {
    private val logger = logger()
    private var watchingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startWatching(autoConversion: AutoConversion) {
        stopWatching()
        val duration = autoConversion.watchDuration ?: return
        logger.info("Starting the watcher")
        watchingJob = scope.launch {
            try {
                while (isActive) {
                    delay(duration)
                    val changes = videoApi.refreshDirs()
                    if (changes) {
                        logger.info("Changes detected, refreshing data")
                        processChanges(autoConversion.conversion)
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    logger.error("Whoops!! ${e.message}", e)
                }
            }
        }
    }

    private suspend fun processChanges(conversion: Map<Codec, Codec>) {
        // Make sure those files are not already in the job queue.
        val codecMap = conversion.mapKeys { it.key.name.lowercase() }
        val changedFiles = repo.getAllVideoFiles()
            .filterNot { conversionApi.jobs.any { j -> j.videoFile.fileName == it.fileName } }
            .filter { it.audios.any { a -> a.codec.lowercase() in codecMap.keys } }
        logger.info("Conversion:$codecMap Changed: ${changedFiles.map { it.fileName }}")
        changedFiles.forEach { processVideoFile(it, codecMap) }
    }

    fun processVideoFile(videoFile: VideoFile, conversion: Map<String, Codec>) {
        val convertTracks = videoFile.audios.filter { it.codec.lowercase() in conversion.keys }
        if (convertTracks.isNotEmpty()) {
            val conversionSpecs = buildMap<MediaTrack, Conversion.Convert> {
                convertTracks.forEach {
                    val targetCodec = conversion[it.codec.lowercase()]
                    if (targetCodec != null) {
                        put(it, Conversion.Convert(targetCodec))
                    } else {
                        logger.warn("Unable to convert ${it.codec}! It is not defined in code")
                    }
                }
            }
            logger.info("Auto Converting for ${videoFile.fileName}")
            conversionApi.startConversion(videoFile, conversionSpecs)
        }
    }

    fun stopWatching() {
        logger.info("Stopping the watcher")
        watchingJob?.cancel()
    }
}