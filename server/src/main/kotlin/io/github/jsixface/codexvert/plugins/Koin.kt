package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.logger.slf4jLogger

fun configureKoin() {
    startKoin {
        slf4jLogger()
        modules(koinModule)
    }
}

private val koinModule = module {
    single { SettingsApi() }
    single { VideoApi() }
    single { BackupApi() }
    single { ConversionApi(settingsApi = get()) }
    single { JobsApi(conversionApi = get()) }
}
