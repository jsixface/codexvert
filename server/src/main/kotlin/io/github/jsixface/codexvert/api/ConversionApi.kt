package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.VideoFile
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.InputStream
import java.io.UncheckedIOException
import java.nio.file.Files
import java.util.UUID
import kotlin.io.path.Path
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class ConversionApi(preferences: IPreferences) {
    private val logger = logger()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val jobs = mutableListOf<ConvertingJob>()
    private val workspace = File(preferences.getSettings().workspaceLocation)

    init {
        if (workspace.isDirectory.not()) {
            workspace.mkdirs()
        }
        scope.launch {
            while (isActive) {
                val pendingJobs = jobs.filter { it.job == null }
                if (pendingJobs.isNotEmpty()) {
                    val nextJob = pendingJobs.first()
                    if (jobs.none { it.job?.isActive == true }) {
                        logger.info("Starting conversion for ${nextJob.videoFile}")
                        with(nextJob) {
                            job = launch { startJob(videoFile, convSpecs, outFile, progress) }
                        }
                    }
                }
                delay(1.seconds)
            }
        }
    }

    fun clearFinished() {
        jobs.removeAll { it.progress.value == -1 || it.progress.value == 100 }
    }

    fun startConversion(file: VideoFile, convSpecs: Map<MediaTrack, Conversion>): Boolean {
        val jobId = UUID.randomUUID().toString()
        val newDir = File(workspace, jobId).apply { mkdirs() }

        // add it to the job queue
        val convJob = ConvertingJob(
            videoFile = file,
            convSpecs = convSpecs,
            outFile = File(newDir, file.fileName), // TODO convert the output file always to mkv
            job = null,
            progress = MutableStateFlow(0),
            jobId = jobId
        )
        jobs.add(convJob)
        logger.info("Queued to convert file ${file.fileName}")
        return true
    }

    private suspend fun startJob(
        file: VideoFile,
        convSpecs: Map<MediaTrack, Conversion>,
        outFile: File,
        updates: MutableStateFlow<Int>
    ) = coroutineScope {
        val builder = ProcessBuilder(*buildCommand(file, convSpecs, outFile).toTypedArray()).redirectErrorStream(true)
        logger.debug("Starting process: {}", builder.command().joinToString(separator = " "))
        val process = builder.start()
        // Read output and update stateflow
        launch {
            parseProcessOut(process.inputStream, updates)
        }

        while (isActive && process.isAlive) {
            // Exits out when conversion is complete or on cancellation, kills the process and exit
            try {
                delay(500.milliseconds)
            } catch (e: CancellationException) {
                logger.info("Killing process id: ${process.pid()} Command: ${process.info().commandLine()}")
                process.destroyForcibly()
            }
        }
        if (process.isAlive) process.destroyForcibly()
        if (process.waitFor() == 0) moveFiles(file, outFile)
    }

    @OptIn(ExperimentalTime::class)
    private fun moveFiles(file: VideoFile, outFile: File) {
        logger.info("Move file to location")
        try {
            val bkpFile = Path("${file.path}.${Clock.System.now().epochSeconds}.bkp")
            logger.info("   Backing up ${file.path}")
            Files.move(Path(file.path), bkpFile)
            logger.info("   Move ${outFile.path} to ${file.path}")
            Files.move(outFile.toPath(), Path(file.path))
            logger.info("   Delete workspace dir. ${outFile.parent}")
            Files.deleteIfExists(outFile.parentFile.toPath())
        } catch (e: Exception) {
            logger.error("Cannot move file", e)
        }
    }

    private fun parseProcessOut(iStream: InputStream, updates: MutableStateFlow<Int>) {
        var duration: Duration? = null
        val durationRegex = Regex("^\\s*Duration: (\\d+):(\\d+):([\\d.]+).*")
        val frameRegex = Regex(".*time=(\\d+):(\\d+):([\\d.]+).*")
        try {
            iStream.bufferedReader().use { s ->
                s.lines().forEach { line ->
                    if (durationRegex.matches(line)) {
                        val dur = durationRegex.replace(line, "PT$1H$2M$3S")
                        duration = Duration.parseIsoStringOrNull(dur)
                    } else if (frameRegex.matches(line)) {
                        val dur = frameRegex.replace(line, "PT$1H$2M$3S")
                        val currentDur = Duration.parseIsoStringOrNull(dur)
                        currentDur?.let { c ->
                            duration?.let { d ->
                                val percent = c.inWholeSeconds * 100 / d.inWholeSeconds
                                updates.value = percent.toInt()
                            }
                        }
                    }
                }
            }
        } catch (_: UncheckedIOException) {
        }
        updates.value = 100
    }

    private fun buildCommand(
        file: VideoFile,
        convSpecs: Map<MediaTrack, Conversion>,
        outFile: File
    ): List<String> = listOf(
        "ffmpeg",
        "-hide_banner",
        "-i",
        file.path,
        *conversionParams(convSpecs, file).toTypedArray(),
        outFile.absolutePath
    )

    private fun conversionParams(convSpecs: Map<MediaTrack, Conversion>, file: VideoFile): List<String> {
        val result = mutableListOf<String>()
        val convIndices = convSpecs.keys.map { it.index }
        val missingTracks = (file.subtitles + file.videos + file.audios).filter { it.index !in convIndices }
        val copyTracks = missingTracks.map { it to Conversion.Copy }
        val allConversion = convSpecs + copyTracks
        allConversion.toList().forEachIndexed { i, (track, conv) ->
            when (conv) {
                Conversion.Copy -> result += listOf(
                    "-map",
                    "0:${track.index}",
                    "-codec:$i", // i = stream number in output file
                    "copy"
                )

                Conversion.Drop -> {}

                is Conversion.Convert -> result += listOf(
                    "-map",
                    "0:${track.index}",
                    "-codec:$i"
                ) + conv.codec.ffmpegParams
            }
        }
        return result
    }
}

data class ConvertingJob @OptIn(ExperimentalTime::class) constructor(
    val videoFile: VideoFile,
    val convSpecs: Map<MediaTrack, Conversion>,
    val outFile: File,
    var job: Job?,
    val progress: MutableStateFlow<Int> = MutableStateFlow(0),
    val jobId: String,
    val startedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)