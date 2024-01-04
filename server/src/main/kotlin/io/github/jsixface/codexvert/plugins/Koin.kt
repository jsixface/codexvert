package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.JobsApi
import io.github.jsixface.codexvert.api.SettingsApi
import io.github.jsixface.codexvert.api.VideoApi
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    startKoin {
        slf4jLogger()
        modules(koinModule)
    }
}

private val koinModule = module {
    single { SettingsApi() }
    single { VideoApi() }
    single { ConversionApi(settingsApi = get()) }
    single { JobsApi(conversionApi = get()) }
}
