package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.IPreferences
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.Codec
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.VideoFile
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.StandardWatchEventKinds.OVERFLOW
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.isDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible

class Watchers(
    private val videoApi: VideoApi,
    private val repo: IVideoFilesRepo,
    private val conversionApi: ConversionApi,
    private val preferences: IPreferences
) {
    private val logger = logger()
    private var watchingJob: Job? = null
    private var watchService: WatchService? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startWatching(autoConversion: AutoConversion) {
        stopWatching()
        logger.info("Starting the watcher")
        watchingJob = scope.launch {
            try {
                val settings = preferences.getSettings()
                FileSystems.getDefault().newWatchService().use { ws ->
                    watchService = ws
                    val keys = mutableMapOf<WatchKey, Path>()

                    fun registerRecursively(root: Path) {
                        if (!root.isDirectory()) return
                        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
                            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                                val key = dir.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
                                keys[key] = dir
                                return FileVisitResult.CONTINUE
                            }
                        })
                    }

                    settings.libraryLocations.forEach { loc -> registerRecursively(Path.of(loc)) }

                    while (isActive) {
                        val key = runInterruptible { ws.take() }
                        val dir = keys[key] ?: continue

                        val changedPaths = mutableSetOf<Path>()
                        for (event in key.pollEvents()) {
                            val kind = event.kind()
                            if (kind == OVERFLOW) continue

                            val namePath = event.context() as? Path ?: continue
                            val child = dir.resolve(namePath)

                            if (kind == ENTRY_CREATE && child.isDirectory()) registerRecursively(child)

                            val isVideoFile = settings.videoExtensions.any { ext ->
                                child.fileName.toString().endsWith(ext, ignoreCase = true)
                            }
                            if (isVideoFile) changedPaths.add(child)
                        }

                        if (changedPaths.isNotEmpty()) {
                            if (videoApi.refreshDirs()) {
                                logger.info("Changes detected, refreshing data")
                                processChanges(autoConversion.conversion, changedPaths)
                            }
                        }

                        if (!key.reset()) {
                            keys.remove(key)
                            if (keys.isEmpty()) break
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException && e !is ClosedWatchServiceException) {
                    logger.error("Whoops!! ${e.message}", e)
                }
            } finally {
                watchService = null
            }
        }
    }

    private suspend fun processChanges(conversion: Map<Codec, Codec>, changedPaths: Set<Path> = emptySet()) {
        // Make sure those files are not already in the job queue.
        val codecMap = conversion.mapKeys { it.key.name.lowercase() }
        val allVideoFiles = if (changedPaths.isEmpty()) {
            repo.getAllVideoFiles()
        } else {
            changedPaths.mapNotNull { repo.getFile(it) }
        }
        val changedFiles = allVideoFiles
            .filterNot { conversionApi.jobs.any { j -> j.videoFile.fileName == it.fileName } }
            .filter { it.audios.any { a -> a.codec.lowercase() in codecMap.keys } }
        logger.info("Conversion:$codecMap Changed: ${changedFiles.map { it.fileName }}")
        changedFiles.forEach { processVideoFile(it, codecMap) }
    }

    fun processVideoFile(videoFile: VideoFile, conversion: Map<String, Codec>) {
        val convertTracks = videoFile.audios.filter { it.codec.lowercase() in conversion.keys }
        if (convertTracks.isNotEmpty()) {
            val conversionSpecs = buildMap {
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
        watchService?.close()
        watchService = null
    }
}