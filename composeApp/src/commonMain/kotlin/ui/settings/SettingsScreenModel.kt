package ui.settings

import io.github.jsixface.common.Api
import io.github.jsixface.common.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.flow
import ui.model.ModelState
import ui.model.ModelState.Error
import ui.model.ModelState.Init
import ui.model.ModelState.Success
import util.log

class SettingsScreenModel(private val client: HttpClient) {

    init {
        log("New SettingsScreenModel")
    }

    val state = flow<ModelState<Settings>> {
        emit(Init())
        val result = client.get(Api.Settings)
        log("Got result: $result")
        when {
            result.status.isSuccess() -> emit(Success(result.body()))
            else -> emit(Error("Error. Status: ${result.status}"))
        }
    }

    suspend fun save(settings: Settings) = client.post(Api.Settings) {
        contentType(ContentType.Application.Cbor)
        setBody(settings)
    }
}