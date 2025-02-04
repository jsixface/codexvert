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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.VideoFile
import org.koin.compose.koinInject
import ui.model.ModelState
import viewmodels.VideoListViewModel

@Composable
fun FileDetails(file: String, onDismiss: (Map<MediaTrack, Conversion>?) -> Unit) {
    val viewModel = koinInject<VideoListViewModel>()
    var videoFile by remember { mutableStateOf<VideoFile?>(null) }
    var errorLoading by remember { mutableStateOf<String?>(null) }
    val conversion = remember { mutableStateMapOf<MediaTrack, Conversion>() }
    LaunchedEffect(file) {
        viewModel.getVideoFile(file).collect {
            when (it) {
                is ModelState.Success -> {
                    videoFile = it.result
                    it.result.audios.forEach { a -> conversion[a] = Conversion.Copy }
                    it.result.videos.forEach { v -> conversion[v] = Conversion.Copy }
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
    OutlinedCard(
        modifier = padder.width(800.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = padder.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            videoFile?.let { file ->
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
                    Button(onClick = { onDismiss(conversion.toMap()) }, modifier = padder) { Text("Convert") }
                    Button(onClick = { onDismiss(null) }, modifier = padder) { Text("Cancel") }
                }
            } ?: Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Loading...", style = MaterialTheme.typography.displayLarge, modifier = padder)
                CircularProgressIndicator(modifier = padder)
            }
        }
    }
}

@Composable
private fun CodecRow(ai: Int, track: MediaTrack, selected: Conversion, onSelect: (Conversion) -> Unit) {
    val codecsAvailable = Codec.entries.filter { it.type == track.type }
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
            codecsAvailable.filter { it.name.lowercase() != track.codec.lowercase() }.forEach { c ->
                val convert = Convert(c)
                FilterChip(
                    onClick = { onSelect(convert) },
                    selected = (selected as? Convert)?.codec == c,
                    label = { Text(c.name) },
                    modifier = Modifier.padding(3.dp, 0.dp),
                )
            }
            FilterChip(
                onClick = { onSelect(Conversion.Drop) },
                selected = (selected == Conversion.Drop),
                label = { Text("DROP") },
                modifier = Modifier.padding(3.dp, 0.dp),
            )
        }
    }
}
