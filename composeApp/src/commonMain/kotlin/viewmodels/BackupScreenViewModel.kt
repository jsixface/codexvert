package viewmodels

import io.github.jsixface.common.Api
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import kotlinx.coroutines.flow.flow
import ui.model.ModelState
import ui.model.ModelState.*
import util.log

class BackupScreenViewModel(private val client: HttpClient) {

    init {
        log("New backupScreenViewModel")
    }

    val backupFiles = flow<ModelState<List<String>>> {
        emit(Init())
        val result = kotlin.runCatching { client.get(Api.Backups) }.getOrNull()
        log("Got result: $result")
        if (result?.status?.isSuccess() == true) emit(Success(result.body()))
        else emit(Error("Error. Status: ${result?.status}"))
    }

    suspend fun delete(backup: String) {
        client.delete(Api.Backups.Backup(path = backup))
    }

    suspend fun clearBackups() {
        client.delete(Api.Backups)
    }
}