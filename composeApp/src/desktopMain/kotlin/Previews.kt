
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import io.github.jsixface.common.*
import ui.*
import ui.home.FileDetails
import ui.home.HomeScreen
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
        SettingsScreen.ListEditor("Elite list", listOf("One", "Two", "Three"), {}, {})
    }
}

@Composable
@Preview
fun seeJobs() {
    AppTheme {
        println(jobs.size)
        JobsScreen.JobContent(jobs, onDelete = {}, onClear = {})
    }
}

@Composable
@Preview
fun seeBackups() {
    AppTheme {
        val backups = listOf("The-Boys-S03E01-Payback-WEBDL-1080p.mkv.1710158838.bkp" )
        BackupsScreen.BackupContent(backups, {}, {})
    }
}

@Composable
@Preview
fun previewBackendDialog() {
    AppTheme {
        BackendDialogContent("helium.home", {}, {})
    }
}


@Composable
@Preview
fun previewMainScreen() {
    AppTheme {
        MainScreen()
    }
}

@Preview
@Composable
private fun seeHomeScreen() {
    AppTheme {
        HomeScreen.content()
    }
}

@Preview
@Composable
private fun seeFileDetails() {
    AppTheme {
        FileDetails(videos[0]) {}
    }
}

@Preview
@Composable
private fun seeVideoScreen() {
    AppTheme {
        HomeScreen.PageContent(videos, {}) {}
    }
}