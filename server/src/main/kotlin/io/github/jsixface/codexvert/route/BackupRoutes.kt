package io.github.jsixface.codexvert.route

import io.github.jsixface.codexvert.api.BackupApi
import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.Api
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.backupRoutes() {

    val logger = logger()
    val backupApi by inject<BackupApi>()

    get<Api.Backups> {
        call.respond(backupApi.getBackups())
    }

    delete<Api.Backups> {
        backupApi.deleteAllBackups()
        call.respond(HttpStatusCode.OK)
    }

    delete<Api.Backups.Backup> { backup ->
        backupApi.deleteBackup(backup.path)
        call.respond(HttpStatusCode.OK)
    }
}