package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.route.backupRoutes
import io.github.jsixface.codexvert.route.jobRoutes
import io.github.jsixface.codexvert.route.videoRoutes
import io.github.jsixface.route.settingsRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticFiles
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
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