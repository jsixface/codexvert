package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.BackupApi
import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.JobsApi
import io.github.jsixface.codexvert.api.SettingsApi
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.db.getDb
import io.ktor.server.application.Application
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    val koinModule = module {
        single { getDb() }
        single { VideoApi() }
        single { BackupApi() }
        single { ConversionApi() }
        single { JobsApi(conversionApi = get()) }
        single(createdAtStart = true) { Watchers(videoApi = get(), conversionApi = get()) }
        single { SettingsApi(watchers = get()) }
    }

    startKoin {
        slf4jLogger()
        modules(koinModule)
    }
}
