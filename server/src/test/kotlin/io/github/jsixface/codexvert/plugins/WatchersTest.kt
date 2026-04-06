package io.github.jsixface.codexvert.plugins

import io.github.jsixface.codexvert.api.ConversionApi
import io.github.jsixface.codexvert.api.IPreferences
import io.github.jsixface.codexvert.api.VideoApi
import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.Codec
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.Settings
import io.github.jsixface.common.VideoFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WatchersTest {

    private val videoApi = mock<VideoApi>()
    private val repo = mock<IVideoFilesRepo>()
    private val conversionApi = mock<ConversionApi>()
    private val preferences = mock<IPreferences>()

    private val watchers = Watchers(
        videoApi = videoApi, repo = repo, conversionApi = conversionApi, preferences = preferences
    )

    @Test
    fun `should convert AAC to MP3`() {
        val videos = videoFile("a.avi", "aac", "eac3")
        watchers.processVideoFile(videos, mapOf("aac" to Codec.MP3))
        verify(conversionApi).startConversion(
            videos, mapOf(videos.audios[0] to Conversion.Convert(Codec.MP3))
        )
    }

    @Test
    fun `should detect file system changes recursively`(): Unit = runBlocking {
        withWatcher { tempDir ->
            val subDir = Files.createDirectory(tempDir.resolve("subdir"))
            val testFile = subDir.resolve("test.mkv")
            val videoFile = videoFile("test.mkv", "aac")
            whenever(repo.getFile(testFile)).thenReturn(videoFile)

            // Ensure registration is complete
            delay(1.seconds)

            // Create a matching file in subdir
            Files.createFile(testFile)

            // Wait for event to be processed
            waitForWatcher(retries = 20) {
                verify(videoApi).refreshDirs()
                verify(repo).getFile(testFile)
                verify(conversionApi).startConversion(
                    videoFile, mapOf(videoFile.audios[0] to Conversion.Convert(Codec.MP3))
                )
            }
        }
    }

    @Test
    fun `should detect file system changes in newly created directory`(): Unit = runBlocking {
        withWatcher { tempDir ->
            val testFile = tempDir.resolve("newdir/test.mkv")
            val videoFile = videoFile("test.mkv", "aac")
            whenever(repo.getFile(testFile)).thenReturn(videoFile)

            // Create a new directory
            Files.createDirectory(tempDir.resolve("newdir"))

            // Wait for the watcher to register the new directory
            delay(2.seconds)

            // Create a matching file in the new directory
            Files.createFile(testFile)

            // Wait for event to be processed
            waitForWatcher(retries = 20) {
                verify(videoApi).refreshDirs()
                verify(repo).getFile(testFile)
                verify(conversionApi).startConversion(
                    videoFile, mapOf(videoFile.audios[0] to Conversion.Convert(Codec.MP3))
                )
            }
        }
    }

    private suspend fun withWatcher(
        autoConversion: AutoConversion = AutoConversion(
            conversion = mapOf(Codec.AAC to Codec.MP3)
        ), block: suspend (Path) -> Unit
    ) {
        val tempDir = Files.createTempDirectory("watchers_test")
        try {
            val settings = Settings(
                libraryLocations = listOf(tempDir.toAbsolutePath().toString()), videoExtensions = listOf(".mkv", ".mp4")
            )
            whenever(preferences.getSettings()).thenReturn(settings)
            whenever(videoApi.refreshDirs()).thenReturn(true)

            watchers.startWatching(autoConversion)
            // Give watch service a moment to start
            delay(2.seconds)

            block(tempDir)

            watchers.stopWatching()
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    private suspend fun waitForWatcher(
        retries: Int = 10, interval: Duration = 1.seconds, verification: suspend () -> Unit
    ) {
        for (i in 1..retries) {
            delay(interval)
            try {
                verification()
                return
            } catch (e: Throwable) {
                if (i == retries) throw e
            }
        }
    }

    @OptIn(ExperimentalTime::class)
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