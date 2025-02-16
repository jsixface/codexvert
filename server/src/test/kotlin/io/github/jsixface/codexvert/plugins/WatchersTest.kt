package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.common.Codec
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.VideoFile
import kotlinx.datetime.Clock
import org.junit.Test

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class WatchersTest {

    private val videoApi = mock<VideoApi>()
    private val repo = mock<IVideoFilesRepo>()
    private val conversionApi = mock<ConversionApi>()

    private val watchers = Watchers(
        videoApi = videoApi,
        repo = repo,
        conversionApi = conversionApi
    )

    @Test
    fun `should convert AAC to MP3`() {
        val videos = videoFile("a.avi", "aac", "eac3")
        watchers.processVideoFile(videos, mapOf("aac" to Codec.MP3))
        verify(conversionApi).startConversion(
            videos,
            mapOf(videos.audios[0] to Conversion.Convert(Codec.MP3))
        )
    }

    private fun videoFile(fileName: String, vararg codecs: String): VideoFile {
        val audios = codecs.mapIndexed { i, c ->
            MediaTrack.AudioTrack(c, i, 2, "", 0, "", "eng")
        }
        return VideoFile(
            path = "path",
            fileName = fileName,
            modifiedTime = Clock.System.now().toEpochMilliseconds(),
            audios = audios,
            videos = emptyList(),
            subtitles = emptyList()
        )
    }
}