package io.github.jsixface.codexvert

import io.github.jsixface.codexvert.plugins.configureHTTP
import io.github.jsixface.codexvert.plugins.configureKoin
import io.github.jsixface.codexvert.plugins.configureRouting
import io.github.jsixface.codexvert.plugins.configureWatchers
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureKoin()
    configureRouting()
    configureHTTP()
    configureWatchers()
}


inline fun <reified T> T.logger(): Logger {
    if (T::class.isCompanion) {
        return LoggerFactory.getLogger(T::class.java.enclosingClass)
    }
    return LoggerFactory.getLogger(T::class.java)
}
