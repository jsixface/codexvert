package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.route.jobRoutes
import io.github.jsixface.codexvert.route.videoRoutes
import io.github.jsixface.codexvert.route.backupRoutes
import io.github.jsixface.route.settingsRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    install(Resources)
    routing {

        staticFiles("/", File("static"))

        jobRoutes()
        videoRoutes()
        backupRoutes()
        settingsRoutes()
    }
}