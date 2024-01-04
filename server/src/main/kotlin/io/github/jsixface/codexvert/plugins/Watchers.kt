package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.SavedData
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.logger
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ApplicationStopping
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import kotlin.io.path.pathString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.inject


fun Application.configureWatchers() {

    val videoApi by inject<VideoApi>()
    val logger = logger()

    val savedData = SavedData.load()
    val firstDir = savedData.settings.libraryLocations.firstOrNull() ?: return
    try {
        val watchService = Paths.get(firstDir).fileSystem.newWatchService()
        monitor.subscribe(ApplicationStopping) { watchService.close() }

        savedData.settings.libraryLocations.forEach {
            Paths.get(it).register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        }
        CoroutineScope(Dispatchers.Default).launch {
            // Start the infinite polling loop
            while (isActive) {
                val key = withContext(Dispatchers.IO) { watchService.take() }
                val directory = (key.watchable() as? Path) ?: continue
                for (event in key.pollEvents()) {
                    val path = (event.context() as? Path) ?: continue
                    val eventFile = File(directory.toFile(), path.pathString)
                    logger.info("file: ${eventFile.absolutePath} has event ${event.kind()}")
                    videoApi.refreshDirs()
                }
                if (!key.reset()) {
                    // Don't have the access to listen on this directory anymore.
                    monitor.raise(ApplicationStopped, this@configureWatchers)
                    break // loop
                }
            }
        }
    } catch (e: Exception) {
        logger.error("Whoops!!", e)
    }
}