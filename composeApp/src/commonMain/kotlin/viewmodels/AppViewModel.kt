package viewmodels

import io.github.jsixface.common.Api
import io.github.jsixface.common.CodecsCollection
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.flow
import ui.model.ModelState
import ui.model.ModelState.Error
import ui.model.ModelState.Init
import ui.model.ModelState.Success

class AppViewModel(private val client: HttpClient) {

    val codecsCollection = flow<ModelState<CodecsCollection>> {
        emit(Init())
        runCatching { client.get(Api.Codecs) }.onSuccess { resp ->
            when {
                resp.status.isSuccess() -> emit(Success(resp.body()))
                else -> emit(Error("Error. Status: ${resp.status}"))
            }
        }.onFailure {
            emit(Error("Error. Status: ${it.message}"))
        }
    }
}