package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.SavedData
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.Codec
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.isDolby
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration

class Watchers(private val videoApi: VideoApi, private val conversionApi: ConversionApi) {
    private val logger = logger()
    private var watchingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Start watching if the settings allow it
        val savedData = SavedData.load()
        savedData.settings.watchDuration?.let { duration ->
            startWatching(duration)
        }
    }

    fun startWatching(interval: Duration) {
        stopWatching()
        logger.info("Starting the watcher")
        watchingJob = scope.launch {
            try {
                while (isActive) {
                    delay(interval)
                    val changes = videoApi.refreshDirs()
                    if (changes) {
                        logger.info("Changes detected, refreshing data")
                        processChanges()
                    }
                }
            } catch (e: Exception) {
                logger.error("Whoops!!", e)
            }
        }
    }

    private fun processChanges() {
        // Convert the files that has EAC3 or AC3 codec to AAC codec.
        // Make sure those files are not already in the job queue.
        val files = videoApi.getVideos()
        files.values.forEach { videoFile ->
            val dolbyTracks = videoFile.audios.filter { it.isDolby() }
            val job = conversionApi.jobs.find { it.videoFile.fileName == videoFile.fileName }
            if (dolbyTracks.isNotEmpty() && job == null) {
                val conversionSpecs = dolbyTracks.associateWith { Conversion.Convert(Codec.AAC) }
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