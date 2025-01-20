package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.BackupApi
import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.JobsApi
import io.github.jsixface.codexvert.api.SettingsApi
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.db.VideoFilesRepo
import io.github.jsixface.codexvert.db.getDb
import io.github.jsixface.codexvert.ffprobe.IParser
import io.github.jsixface.codexvert.ffprobe.Parser
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    val koinModule = module {
        single<Database> { getDb() }
        singleOf(::VideoFilesRepo) bind IVideoFilesRepo::class
        singleOf(::Parser) bind IParser::class
        singleOf(::VideoApi)
        singleOf(::BackupApi)
        singleOf(::ConversionApi)
        singleOf(::JobsApi)
        singleOf(::SettingsApi)
        singleOf(::Watchers) { createdAtStart() }
    }

    startKoin {
        slf4jLogger()
        modules(koinModule)
    }
}
