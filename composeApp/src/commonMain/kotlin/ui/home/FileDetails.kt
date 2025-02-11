package ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jsixface.common.Codec
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.Conversion.Convert
import io.github.jsixface.common.Conversion.Copy
import io.github.jsixface.common.Conversion.Drop
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.TrackType
import io.github.jsixface.common.VideoFile
import org.koin.compose.koinInject
import ui.model.ModelState
import util.log
import viewmodels.VideoListViewModel

@Composable
fun FileDetails(file: String, onDismiss: (Map<MediaTrack, Conversion>?) -> Unit) {
    val viewModel = koinInject<VideoListViewModel>()
    var videoFile by remember { mutableStateOf<VideoFile?>(null) }
    var errorLoading by remember { mutableStateOf<String?>(null) }
    val conversion = remember { mutableStateMapOf<MediaTrack, Conversion>() }
    LaunchedEffect(file) {
        // re-initialize after loading a different file
        videoFile = null
        errorLoading = null
        conversion.clear()

        // Load file details
        viewModel.getVideoFile(file).collect {
            when (it) {
                is ModelState.Success -> {
                    videoFile = it.result
                    it.result.audios.forEach { a -> conversion[a] = Copy }
                    it.result.videos.forEach { v -> conversion[v] = Copy }
                }

                is ModelState.Error<*> -> {
                    errorLoading = it.msg
                }

                is ModelState.Init<*> -> {
                    videoFile = null
                }
            }
        }
    }

    val padder = Modifier.padding(16.dp)
    Column(
        modifier = padder.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        videoFile?.let { file ->
            Row {
                Text(file.fileName, style = MaterialTheme.typography.displaySmall, modifier = padder)
            }
            val tracks = file.videos + file.audios + file.subtitles
            if (tracks.isNotEmpty()) tracks.forEach { track ->
                CodecRow(track, conversion[track] ?: Copy) {
                    log("Adding track $track with conversion $it")
                    conversion[track] = it
                }
            }

            Row {
                Button(onClick = { onDismiss(conversion.toMap()) }, modifier = padder) { Text("Convert") }
                Button(onClick = { onDismiss(null) }, modifier = padder) { Text("Cancel") }
            }
        } ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Loading...", style = MaterialTheme.typography.displayLarge, modifier = padder)
            CircularProgressIndicator(modifier = padder)
        }
    }
}


@Composable
private fun CodecRow(track: MediaTrack, selected: Conversion, onSelect: (Conversion) -> Unit) {
    val codecsAvailable = getAvailableConversion(track).filterNot { it.name.lowercase() == track.codec.lowercase() }
    val actions = buildMap {
        put("AS IS", Copy)
        put("DELETE", Drop)
        codecsAvailable.forEach { put(it.name, Convert(it)) }
    }
    Row(
        modifier = Modifier.padding(16.dp, 4.dp)
            .border(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(16.dp))
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Text(track.typeString())
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "TO")

        Row(verticalAlignment = Alignment.CenterVertically) {
            actions.forEach { (name, conv) ->
                FilterChip(
                    onClick = { onSelect(conv) },
                    selected = selected == conv,
                    label = { Text(name) },
                    modifier = Modifier.padding(3.dp, 0.dp),
                )
            }
        }
    }
}

private fun MediaTrack.typeString() = when (this) {
    is MediaTrack.AudioTrack -> "Audio: $codec  ${language.uppercase()}"
    is MediaTrack.SubtitleTrack -> "Subtitle: $codec  ${language.uppercase()}"
    is MediaTrack.VideoTrack -> "Video: $codec"
}

private fun getAvailableConversion(track: MediaTrack): List<Codec> = when (track) {
    is MediaTrack.VideoTrack -> Codec.entries.filter { it.type == TrackType.Video }
    is MediaTrack.AudioTrack -> Codec.entries.filter { it.type == TrackType.Audio }
    is MediaTrack.SubtitleTrack -> Codec.entries.filter { it.type == TrackType.Subtitle }
}