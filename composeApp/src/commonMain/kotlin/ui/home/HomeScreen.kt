package ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jsixface.common.VideoFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ui.model.ModelState
import ui.model.Screen
import ui.utils.ComboBox
import viewmodels.VideoListViewModel


object HomeScreen : Screen {

    private var filteredAudioCodec by mutableStateOf<String?>(null)
    private var filteredVideoCodec by mutableStateOf<String?>(null)
    private var filteredName by mutableStateOf("")
    private var selectedVideo by mutableStateOf<VideoFile?>(null)
    private var showFileDetails by mutableStateOf(false)
    private val bottomPad = Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp)
    private val sidePad = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp)

    override val name: String
        get() = "Home"

    @Composable
    override fun icon() {
        Icon(Icons.Filled.Home, contentDescription = name)
    }

    @Composable
    override fun content() {

        var loadingJob: Job? by remember { mutableStateOf(null) }
        var loading by remember { mutableStateOf(true) }
        var errorLoading by remember { mutableStateOf(false) }
        var videoList by remember { mutableStateOf(listOf<VideoFile>()) }
        val viewModel = koinInject<VideoListViewModel>()
        val scope = rememberCoroutineScope()

        fun load() {
            loadingJob?.cancel()
            loadingJob = scope.launch {
                viewModel.videoList.collect {
                    when (it) {
                        is ModelState.Init -> {
                            loading = true
                            errorLoading = false
                        }

                        is ModelState.Error -> {
                            loading = false
                            errorLoading = true
                        }

                        is ModelState.Success -> {
                            loading = false
                            errorLoading = false
                            videoList = it.result
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) { load() }

        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            // Heading & status
            Row(modifier = bottomPad) {
                Text(
                    "Video files",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = sidePad
                )
                if (loading) {
                    CircularProgressIndicator(modifier = sidePad)
                }
                if (errorLoading) {
                    Text(
                        "Error loading list",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (showFileDetails) {
                selectedVideo?.let { v ->
                    FileDetailsDialog(v) { conv ->
                        showFileDetails = false
                        conv?.let { scope.launch { viewModel.submitJob(v, conv) } }
                    }
                }
            }
            PageContent(videoList, videoSelected = {
                selectedVideo = it
                showFileDetails = true
            }) {
                scope.launch {
                    loading = true
                    viewModel.refresh()
                    load()
                }
            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun PageContent(list: List<VideoFile>, videoSelected: (VideoFile) -> Unit, onRefresh: () -> Unit) {
        val filteredVideos =
            list.filter {
                it.fileName.contains(filteredName, ignoreCase = true)
                    && it.videos.any { v -> filteredVideoCodec?.let { fv -> v.codec == fv } ?: true }
                    && it.audios.any { a -> filteredAudioCodec?.let { fa -> a.codec == fa } ?: true }
            }
        Column {
            Row(modifier = bottomPad.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = filteredName,
                    modifier = sidePad,
                    onValueChange = { filteredName = it },
                    label = { Text("File name") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") })
                val videoOptions = list.asSequence().flatMap { it.videos }.map { it.codec }.toSet().toList().sorted()
                ComboBox("Video Codecs", videoOptions, filteredVideoCodec) { filteredVideoCodec = it }
                val audioOptions = list.asSequence().flatMap { it.audios }.map { it.codec }.toSet().toList().sorted()
                ComboBox("Audio Codecs", audioOptions, filteredAudioCodec) { filteredAudioCodec = it }
                IconButton(modifier = sidePad, onClick = onRefresh) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                }
                // Clear filters
                if (filteredName.isNotEmpty() || filteredAudioCodec != null || filteredVideoCodec != null) {
                    IconButton(onClick = {
                        filteredName = ""
                        filteredAudioCodec = null
                        filteredVideoCodec = null
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear filters")
                    }
                }
            }
            Row(modifier = bottomPad.fillMaxSize()) {
                LazyColumn {
                    stickyHeader {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text(
                                "File name",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(4f)
                            )
                            Text(
                                "Video Codecs",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Audio Codecs",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    items(filteredVideos) { file ->
                        VideoRow(file) { videoSelected(it) }
                    }
                }
            }
        }
    }

    @Composable
    private fun VideoRow(file: VideoFile, onClick: (VideoFile) -> Unit) {
        Column {
            Surface(
                onClick = { onClick(file) },
                tonalElevation = 3.dp,
                modifier = Modifier.hoverable(MutableInteractionSource())
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Text(file.fileName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(4f))
                    Text(file.videoInfo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(file.audioInfo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
//                Divider()
            }
        }
    }
}
