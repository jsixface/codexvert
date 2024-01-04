package viewmodels

import io.github.jsixface.common.Api
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.VideoFile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.patch
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

class VideoListViewModel(private val client: HttpClient) {
    init {
        log("New VideoListViewModel")
    }

    val videoList = flow<ModelState<List<VideoFile>>> {
        emit(Init())
        runCatching { client.get(Api.Videos) }.onSuccess { resp ->
            when {
                resp.status.isSuccess() -> emit(Success(resp.body()))
                else -> emit(Error("Error. Status: ${resp.status}"))
            }
        }.onFailure {
            emit(Error("Error. Status: ${it.message}"))
        }
    }

    suspend fun submitJob(videoFile: VideoFile, conversions: Map<MediaTrack, Conversion>) {
        client.post(Api.Videos.Video(path = videoFile.fileName)) {
            setBody(conversions.entries.map { it.toPair() })
            contentType(ContentType.Application.Cbor)
        }
    }

    suspend fun refresh() {
        client.patch(Api.Videos).status
    }
}