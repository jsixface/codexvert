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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import io.github.jsixface.common.CodecsCollection
import io.github.jsixface.common.VideoList
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

    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

    var loadingJob: Job? by remember { mutableStateOf(null) }
    var loading by remember { mutableStateOf(true) }
    var errorLoading by remember { mutableStateOf(false) }
    var videoList by remember {
        mutableStateOf(
            VideoList(emptyMap(), CodecsCollection(emptyList(), emptyList(), emptyList()))
        )
    }
    val viewModel = koinInject<VideoListViewModel>()
    val scope = rememberCoroutineScope()

    fun load(audioFilter: String? = null, videoFilter: String? = null) {
        loadingJob?.cancel()
        loadingJob = scope.launch {
            viewModel.videoList(audioFilter, videoFilter).collect {
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
                    PageContent(
                        videoList,
                        updateFilter = { audioCodec, videoCodec -> load(audioCodec, videoCodec) },
                        videoSelected = {
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                            }
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
                navigator.currentDestination?.contentKey?.let { v ->
                    FileDetails(v) { conv ->
                        scope.launch {
                            conv?.let { viewModel.submitJob(v, conv) }
                            navigator.navigateBack()
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun PageContent(
    list: VideoList,
    updateFilter: (String?, String?) -> Unit,
    videoSelected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var filteredAudioCodec by remember { mutableStateOf<String?>(null) }
    var filteredVideoCodec by remember { mutableStateOf<String?>(null) }
    var filteredName by remember { mutableStateOf("") }

    val filteredVideos =
        list.pathAndNames.filter { it.value.contains(filteredName, ignoreCase = true) }.map { it.key to it.value }
    Column {
        val filterMod = Modifier.padding(8.dp, 0.dp).fillMaxWidth()
        Column(modifier = filterMod, horizontalAlignment = Alignment.CenterHorizontally) {

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
            ComboBox("Video Codecs", list.codecsCollection.video, filteredVideoCodec, modifier = filterMod) {
                filteredVideoCodec = it
                updateFilter(filteredAudioCodec, filteredVideoCodec)
            }
            ComboBox("Audio Codecs", list.codecsCollection.audio, filteredAudioCodec, modifier = filterMod) {
                filteredAudioCodec = it
                updateFilter(filteredAudioCodec, filteredVideoCodec)
            }
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(modifier = sidePad, onClick = onRefresh) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                }
                // Clear filters
                if (filteredName.isNotEmpty() || filteredAudioCodec != null || filteredVideoCodec != null) {
                    OutlinedButton(onClick = {
                        filteredName = ""
                        filteredAudioCodec = null
                        filteredVideoCodec = null
                        updateFilter(filteredAudioCodec, filteredVideoCodec)
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear filters")
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            LazyColumn {
                items(filteredVideos) { file ->
                    VideoRow(file.second) { videoSelected(file.first) }
                }
            }
        }
    }
}

@Composable
private fun VideoRow(file: String, onClick: (String) -> Unit) {
    Card(
        onClick = { onClick(file) },
        modifier = Modifier.fillMaxWidth().padding(4.dp).hoverable(MutableInteractionSource()),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = file,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
        }
    }
}
