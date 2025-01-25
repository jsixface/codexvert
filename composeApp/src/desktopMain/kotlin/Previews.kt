
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.ConversionJob
import io.github.jsixface.common.JobStatus
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.TrackType
import io.github.jsixface.common.VideoFile
import ui.BackupContent
import ui.JobContent
import ui.settings.AutoConvertSettings
import ui.settings.BackendDialogContent
import ui.settings.ListEditor
import ui.theme.AppTheme
import kotlin.random.Random


private val videos = listOf(
    VideoFile(
        "Friends.S05E01.DVDrip.XviD-SAiNTS.avi",
        "Friends.S05E01.DVDrip.XviD-SAiNTS.avi",
        234,
        audios = listOf(
            MediaTrack(TrackType.Audio, 0, "mp3"),
            MediaTrack(TrackType.Audio, 1, "aac")
        ),
        videos = listOf(
            MediaTrack(TrackType.Video, 0, "hevc"),
            MediaTrack(TrackType.Video, 1, "mp4")
        )
    ),
    VideoFile(
        "Friends.S05E02.DVDrip.XviD-SAiNTS.avi",
        "Friends.S05E02.DVDrip.XviD-SAiNTS.avi",
        234,
        audios = listOf(
            MediaTrack(TrackType.Audio, 0, "mp3"),
            MediaTrack(TrackType.Audio, 1, "aac")
        ),
        videos = listOf(
            MediaTrack(TrackType.Video, 0, "hevc"),
            MediaTrack(TrackType.Video, 1, "mp4")
        )
    )
)

@OptIn(ExperimentalStdlibApi::class)
private val jobs = videos.mapIndexed { i, v ->
    ConversionJob(
        Random.nextLong().toHexString(),
        status = JobStatus.entries.get(i+2),
        progress = Random.nextInt(100),
        v,
        "2023-02-03"
    )
}

@Composable
@Preview
fun seeSetting() {
    AppTheme {
        ListEditor("Elite list", listOf("One", "Two", "Three"), {}, {})
    }
}

@Composable
@Preview
fun seeJobs() {
    AppTheme {
        println(jobs.size)
        JobContent(jobs, onDelete = {}, onClear = {})
    }
}

@Composable
@Preview
fun seeBackups() {
    AppTheme {
        val backups = listOf("The-Boys-S03E01-Payback-WEBDL-1080p.mkv.1710158838.bkp" )
        BackupContent(backups, {}, {})
    }
}

@Composable
@Preview
fun previewBackendDialog() {
    AppTheme {
        BackendDialogContent("helium.home", {}, {})
    }
}

@Preview
@Composable
fun previewListEditor() {
    AppTheme {
        ListEditor("List title", listOf("One", "Two", "Three"), {}, {})
    }
}

@Composable
@Preview
fun previewAutoConversion() {
    AppTheme {
        AutoConvertSettings(setting = AutoConversion())
    }
}