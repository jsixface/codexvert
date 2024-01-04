package ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.jsixface.common.AudioCodecs
import io.github.jsixface.common.Conversion
import io.github.jsixface.common.Conversion.Convert
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.TrackType
import io.github.jsixface.common.VideoCodecs
import io.github.jsixface.common.VideoFile

@Composable
fun FileDetailsDialog(file: VideoFile, onDismiss: (Map<MediaTrack, Conversion>?) -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss(null) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FileDetails(file) { onDismiss(it) }
    }
}

@Composable
fun FileDetails(file: VideoFile, onDismiss: (Map<MediaTrack, Conversion>?) -> Unit) {
    val conversion = remember {
        mutableStateMapOf<MediaTrack, Conversion>().apply {
            file.audios.forEach { put(it, Conversion.Copy) }
            file.videos.forEach { put(it, Conversion.Copy) }
        }
    }

    val padder = Modifier.padding(16.dp)
    Card(
        modifier = padder.width(800.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = padder.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                Text(file.fileName, style = MaterialTheme.typography.displaySmall, modifier = padder)
            }

            if (file.audios.isNotEmpty()) Text("Audio Tracks", style = MaterialTheme.typography.bodyLarge)
            LazyColumn {
                itemsIndexed(file.audios) { i, track ->
                    CodecRow(i, track, conversion[track] ?: Conversion.Copy) { conversion[track] = it }
                }
            }

            if (file.videos.isNotEmpty()) Text("Video Tracks", style = MaterialTheme.typography.bodyLarge)
            LazyColumn {
                itemsIndexed(file.videos) { i, track ->
                    CodecRow(i, track, conversion[track] ?: Conversion.Copy) { conversion[track] = it }
                }
            }

            Row {
                Button(onClick = { onDismiss(null) }, modifier = padder) { Text("Cancel") }
                Button(onClick = { onDismiss(conversion.toMap()) }, modifier = padder) { Text("Convert") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodecRow(ai: Int, track: MediaTrack, selected: Conversion, onSelect: (Conversion) -> Unit) {
    val codecsAvailable = if (track.type == TrackType.Audio) AudioCodecs else VideoCodecs
    Row(modifier = Modifier.padding(16.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Track $ai", modifier = Modifier.weight(1f))
        Row(modifier = Modifier.weight(2f), verticalAlignment = Alignment.CenterVertically) {
            Text("Codec: ${track.codec}")
        }
        Row(modifier = Modifier.weight(5f), verticalAlignment = Alignment.CenterVertically) {
            Text("To:")
            FilterChip(
                onClick = { onSelect(Conversion.Copy) },
                selected = (selected == Conversion.Copy),
                label = { Text("KEEP") },
                modifier = Modifier.padding(3.dp, 0.dp),
            )
            codecsAvailable.filter { it != track.codec }.forEach { c ->
                val convert = Convert(c)
                FilterChip(
                    onClick = { onSelect(convert) },
                    selected = (selected as? Convert)?.codec == c,
                    label = { Text(c) },
                    modifier = Modifier.padding(3.dp, 0.dp)
                )
            }
            FilterChip(
                onClick = { onSelect(Conversion.Drop) },
                selected = (selected == Conversion.Drop),
                label = { Text("DROP") },
                modifier = Modifier.padding(3.dp, 0.dp)
            )
        }
    }
}
