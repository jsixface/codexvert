package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.SavedData
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.utils.toVideoFile
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.Codec
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.MediaTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Watchers(
    private val videoApi: VideoApi,
    private val repo: IVideoFilesRepo,
    private val conversionApi: ConversionApi
) {
    private val logger = logger()
    private var watchingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Start watching if the settings allow it
        startWatching(SavedData.load().settings.autoConversion)
    }

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
                logger.error("Whoops!!", e)
            }
        }
    }

    private suspend fun processChanges(conversion: Map<String, String>) {
        // Convert the files that has EAC3 or AC3 codec to AAC codec.
        // Make sure those files are not already in the job queue.
        val files = repo.getAll().map { it.toVideoFile() }
        files.forEach { videoFile ->
            val codecMap: Map<String, Codec> = Codec.entries.associateBy { it.name.lowercase() }
            val convertTracks = videoFile.audios.filter { it.codec.lowercase() in conversion.keys }
            val job = conversionApi.jobs.find { it.videoFile.fileName == videoFile.fileName }
            if (convertTracks.isNotEmpty() && job == null) {
                val conversionSpecs = buildMap<MediaTrack, Conversion.Convert> {
                    convertTracks.forEach {
                        val codec = codecMap[it.codec.lowercase()]
                        if (codec != null) {
                            put(it, Conversion.Convert(codec))
                        } else {
                            logger.warn("Unable to convert ${it.codec}! It is not defined in code")
                        }
                    }
                }
                logger.info("Auto Converting for ${videoFile.fileName}")
                conversionApi.startConversion(videoFile, conversionSpecs)
            }
        }
    }

    fun stopWatching() {
        logger.info("Stopping the watcher")
        watchingJob?.cancel()
    }
}