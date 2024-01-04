package io.github.jsixface.codexvert.plugins

import io.ktor.server.application.Application


fun Application.configureWatchers() {
//
//    val videoApi by inject<VideoApi>()
//    val logger = logger()
//
//    val savedData = SavedData.load()
//    val firstDir = savedData.settings.libraryLocations.firstOrNull() ?: return
//    try {
//        val watchService = Paths.get(firstDir).fileSystem.newWatchService()
//        environment.monitor.subscribe(ApplicationStopping) { watchService.close() }
//        savedData.settings.libraryLocations.forEach {
//            Paths.get(it).register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
//        }
//        CoroutineScope(Dispatchers.Default).launch {
//            // Start the infinite polling loop
//            while (isActive) {
//                val key = withContext(Dispatchers.IO) { watchService.take() }
//                val directory = (key.watchable() as? Path) ?: continue
//                for (event in key.pollEvents()) {
//                    val path = (event.context() as? Path) ?: continue
//                    val eventFile = File(directory.toFile(), path.pathString)
//
//                    logger.info("file: ${eventFile.absolutePath} has event ${event.kind()}")
//
//                    videoApi.refreshDirs()
//                }
//                if (!key.reset()) {
//                    // Don't have the access to listen on this directory anymore.
//                    environment.monitor.raise(ApplicationStopped, this@configureWatchers)
//                    break // loop
//                }
//            }
//        }
//    } catch (e: Exception) {
//        logger.error("Whoops!!", e)
//    }
}