package io.github.jsixface.codexvert.route

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.Api
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.MediaTrack
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject


fun Route.videoRoutes() {

    val logger = logger()
    val videoApi by inject<VideoApi>()
    val conversionApi by inject<ConversionApi>()

    get<Api.Videos> { v ->
        call.respond(videoApi.getVideos(v.audioFilter, v.videoFilter))
    }

    patch<Api.Videos> {
        videoApi.refreshDirs()
        call.respond("OK")
    }

    get<Api.Videos.Video> { video ->
        logger.info("Getting video ${video.path}")
        video.path?.let {
            val find = videoApi.getVideo(it)
            logger.info("found = $find")
            call.respondNullable(find)
        } ?: run {
            call.respondRedirect("/videos")
        }
    }

    post<Api.Videos.Video> { video ->
        val videoFilePath = video.path ?: run {
            logger.warn("no path in URL")
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val videoFile = videoApi.getVideo(videoFilePath) ?: run {
            logger.warn("No videos found by name $videoFilePath")
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        logger.info("Converting the video: ${video.path}")
        val data = call.receive<Map<MediaTrack, Conversion>>()
        logger.info("Got the data: $data")
        conversionApi.startConversion(videoFile, data)
        call.respond("OK")
    }

    get<Api.Codecs> {
        call.respond(videoApi.getCodecsPresent())
    }
}
