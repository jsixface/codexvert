package io.github.jsixface.codexvert.plugins

import Greeting
import io.github.jsixface.codexvert.route.jobRoutes
import io.github.jsixface.route.settingsRoutes
import io.github.jsixface.route.videoRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticFiles
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureRouting() {
    install(Resources)
    routing {
        route("/api") {
            get("/") {
                call.respondText(
                    "Ktor: ${Greeting().greet()}. \n" +
                            "Current Dir: ${File(".").absolutePath}"
                )
            }
        }

        staticFiles("/", File("static"))

        jobRoutes()
        videoRoutes()
        settingsRoutes()
    }
}