package viewmodels

import io.github.jsixface.common.Api
import io.github.jsixface.common.JobsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.http.isSuccess
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import ui.model.ModelState
import ui.model.ModelState.Error
import ui.model.ModelState.Init
import ui.model.ModelState.Success
import util.log

class JobsScreenModel(private val client: HttpClient) {
    private val _page = MutableStateFlow(1)
    val page = _page.asStateFlow()

    private val _itemsPerPage = MutableStateFlow(10)
    val itemsPerPage = _itemsPerPage.asStateFlow()

    fun setPage(page: Int) {
        _page.value = page
    }

    fun setItemsPerPage(limit: Int) {
        _itemsPerPage.value = limit
        _page.value = 1
    }

    init {
        log("New JobsScreenModel")
    }

    val jobsResponse = flow<ModelState<JobsResponse>> {
        emit(Init())
        while (true) {
            val result =
                kotlin.runCatching { client.get(Api.Jobs(page = _page.value, limit = _itemsPerPage.value)) }.getOrNull()
            log("Got result: $result")
            if (result?.status?.isSuccess() == true) emit(Success(result.body()))
            else emit(Error("Error. Status: ${result?.status}"))
            delay(1.seconds)
        }
    }

    suspend fun delete(jobId: String) {
        client.delete(Api.Jobs.Job(id = jobId))
    }
}