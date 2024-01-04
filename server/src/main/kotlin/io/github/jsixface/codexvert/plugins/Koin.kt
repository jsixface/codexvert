package io.github.jsixface.codexvert.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(koinModule)
    }

}

private val koinModule = module {
//    single { SettingsApi() }
//    single { VideoApi() }
//    single { ConversionApi(settingsApi = get()) }
//    single { JobsApi(conversionApi = get()) }
}
