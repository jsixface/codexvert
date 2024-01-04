package viewmodels

import io.github.jsixface.common.Api
import io.github.jsixface.common.ConversionJob
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.http.isSuccess
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import ui.model.ModelState
import ui.model.ModelState.Error
import ui.model.ModelState.Init
import ui.model.ModelState.Success
import util.log

class JobsScreenModel(private val client: HttpClient) {

    init {
        log("New JobsScreenModel")
    }

    val jobs = flow<ModelState<List<ConversionJob>>> {
        emit(Init())
        while (true) {
            val result = kotlin.runCatching { client.get(Api.Jobs) }.getOrNull()
            log("Got result: $result")
            if (result?.status?.isSuccess() == true) emit(Success(result.body()))
            else emit(Error("Error. Status: ${result?.status}"))
            delay(1.seconds)
        }
    }

    suspend fun delete(jobId: String) {
        client.delete(Api.Jobs.Job(id = jobId))
    }

    suspend fun clearJobs() {
        client.delete(Api.Jobs)
    }
}