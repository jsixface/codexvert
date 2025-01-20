package ui.home

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import getClientConfig
import io.github.jsixface.common.VideoFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ui.model.ModelState
import ui.utils.ComboBox
import viewmodels.VideoListViewModel


private val sidePad = Modifier.padding(8.dp, 0.dp, 0.dp, 0.dp)


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeScreen() {

    val navigator = rememberListDetailPaneScaffoldNavigator<VideoFile>()

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

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Column {
                    if (loading) CircularProgressIndicator(modifier = sidePad)
                    if (errorLoading) {
                        Text(
                            "Error loading list",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    PageContent(videoList, videoSelected = {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                    }) {
                        scope.launch {
                            loading = true
                            viewModel.refresh()
                            load()
                        }
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                navigator.currentDestination?.content?.let { v ->
                    FileDetails(v) { conv ->
                        conv?.let { scope.launch { viewModel.submitJob(v, conv) } }
                        navigator.navigateBack()
                    }
                }
            }
        }
    )
}


@Composable
fun PageContent(list: List<VideoFile>, videoSelected: (VideoFile) -> Unit, onRefresh: () -> Unit) {
    var filteredAudioCodec by remember { mutableStateOf<String?>(null) }
    var filteredVideoCodec by remember { mutableStateOf<String?>(null) }
    var filteredName by remember { mutableStateOf("") }

    val filteredVideos = list.filter {
        it.fileName.contains(
            filteredName,
            ignoreCase = true
        ) && it.videos.any { v ->
            filteredVideoCodec?.let { fv -> v.codec == fv } ?: true
        } && it.audios.any { a -> filteredAudioCodec?.let { fa -> a.codec == fa } ?: true }
    }
    Column {
        val filterMod = Modifier.padding(8.dp, 0.dp).fillMaxWidth()
        Column(modifier = filterMod, horizontalAlignment = Alignment.CenterHorizontally) {
            val videoOptions = list.asSequence().flatMap { it.videos }.map { it.codec }.toSet().toList().sorted()
            val audioOptions = list.asSequence().flatMap { it.audios }.map { it.codec }.toSet().toList().sorted()

            @OptIn(ExperimentalComposeUiApi::class)
            if (getClientConfig().isDebugEnabled()) {
                val windowInfo = currentWindowAdaptiveInfo()
                val width = windowInfo.windowSizeClass.windowWidthSizeClass.toString().split(" ")[1]
                val height = windowInfo.windowSizeClass.windowHeightSizeClass.toString().split(" ")[1]
                Text("Window Width = $width")
                Text("Window Height = $height")
                Text("Window size = ${LocalWindowInfo.current.containerSize.toSize()}")
                Text("density = ${LocalDensity.current.density}")
            }
            OutlinedTextField(
                value = filteredName,
                modifier = filterMod,
                onValueChange = { filteredName = it },
                label = { Text("File name") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") })
            ComboBox("Video Codecs", videoOptions, filteredVideoCodec, modifier = filterMod) { filteredVideoCodec = it }
            ComboBox("Audio Codecs", audioOptions, filteredAudioCodec, modifier = filterMod) { filteredAudioCodec = it }
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
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
        }
        Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            LazyColumn {
                items(filteredVideos) { file ->
                    VideoRow(file) { videoSelected(it) }
                }
            }
        }
    }
}

@Composable
private fun VideoRow(file: VideoFile, onClick: (VideoFile) -> Unit) {
    Card(
        onClick = { onClick(file) },
        modifier = Modifier.fillMaxWidth().padding(4.dp).hoverable(MutableInteractionSource()),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text(text = file.videoInfo, style = MaterialTheme.typography.bodyMedium)
                Text(text = file.audioInfo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
