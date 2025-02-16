package io.github.jsixface.codexvert

import io.github.jsixface.codexvert.api.IPreferences
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.plugins.Watchers
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun Application.initialize() {
    val videoApi by inject<VideoApi>()
    val watchers by inject<Watchers>()
    val preferences by inject<IPreferences>()
    log.info("initialize application...")

    watchers.startWatching(preferences.getSettings().autoConversion)

    CoroutineScope(Dispatchers.IO).launch {
        videoApi.updateCache()
    }
}
