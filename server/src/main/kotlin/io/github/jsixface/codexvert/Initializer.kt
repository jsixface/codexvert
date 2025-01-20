package io.github.jsixface.codexvert

import io.github.jsixface.codexvert.api.VideoApi
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun Application.initialize() {
    val videoApi by inject<VideoApi>()
    log.info("initialize application...")

    CoroutineScope(Dispatchers.IO).launch {
        videoApi.updateCache()
    }
}