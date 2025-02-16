package io.github.jsixface.codexvert.route

import io.github.jsixface.codexvert.api.SettingsApi
import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.Api
import io.github.jsixface.common.Settings
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.settingsRoutes() {
    val settingsApi by inject<SettingsApi>()
    val logger = logger()

    get<Api.Settings> {
        val settings = settingsApi.getSettings()
        call.respond(settings)
    }

    post<Api.Settings> {
        val settings = call.receive<Settings>()
        logger.info("Got new Settings: $settings")
        settingsApi.saveSettings(settings)
        call.respond(settingsApi.getSettings())
    }
}