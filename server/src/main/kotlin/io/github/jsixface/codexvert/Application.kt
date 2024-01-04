package io.github.jsixface.codexvert

import io.github.jsixface.codexvert.plugins.configureHTTP
import io.github.jsixface.codexvert.plugins.configureKoin
import io.github.jsixface.codexvert.plugins.configureRouting
import io.github.jsixface.codexvert.plugins.configureWatchers
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureHTTP()
    configureKoin()
    configureWatchers()
}
