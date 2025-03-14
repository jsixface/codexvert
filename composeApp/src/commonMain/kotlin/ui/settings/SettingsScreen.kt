package ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.Settings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ui.model.ModelState

private val padding = Modifier.padding(16.dp)
private val paddingSmall = Modifier.padding(8.dp)

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val settingsModel = koinInject<SettingsScreenModel>()
        val scope = rememberCoroutineScope()
        var loading by remember { mutableStateOf(true) }
        var errorLoading by remember { mutableStateOf(false) }
        var workspace by remember { mutableStateOf("") }
        val extensions = remember { mutableStateListOf<String>() }
        val locations = remember { mutableStateListOf<String>() }
        var autoConversion by remember { mutableStateOf(AutoConversion()) }


//        if (getClientConfig().isDebugEnabled()) {
//            val clientConfig = getClientConfig()
//            ClientDialog(clientConfig.backendHost, {}) { clientConfig.backendHost = it }
//        }

        LaunchedEffect(Unit) {
            scope.launch {
                settingsModel.state.collect { state ->
                    when (state) {
                        is ModelState.Error -> {
                            loading = false
                            errorLoading = true
                        }

                        is ModelState.Init -> {
                            loading = true
                        }

                        is ModelState.Success -> {
                            loading = false
                            val settings = state.result
                            val locationsToAdd = settings.libraryLocations.filterNot {
                                locations.contains(it)
                            }
                            locations.addAll(locationsToAdd)
                            val extsToAdd = settings.videoExtensions.filterNot {
                                extensions.contains(it)
                            }
                            extensions.addAll(extsToAdd)
                            workspace = settings.workspaceLocation
                            autoConversion = settings.autoConversion
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.width(width = 600.dp).padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Settings",
                    modifier = padding,
                    style = MaterialTheme.typography.displayLarge,
                )
                if (loading) CircularProgressIndicator(modifier = padding.width(30.dp))
            }
            Column {
                ListEditor("Locations", locations, { locations.remove(it) }, { locations.add(it) })
                HorizontalDivider()
                ListEditor("Extensions", extensions, { extensions.remove(it) }, { extensions.add(it) })
                HorizontalDivider()
                OutlinedTextField(
                    value = workspace,
                    onValueChange = { workspace = it },
                    modifier = padding,
                    label = { Text("Workspace Location") })
                HorizontalDivider()
                AutoConvertSettings(autoConversion) { autoConversion = it }
            }
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    scope.launch {
                        settingsModel.save(
                            Settings(
                                libraryLocations = locations,
                                workspaceLocation = workspace,
                                videoExtensions = extensions,
                                autoConversion = autoConversion
                            )
                        )
                    }
                }, modifier = padding) {
                    Text("Save Settings")
                }
            }
        }
    }
}

@Composable
fun ListEditor(
    title: String, items: List<String>, onDelete: (String) -> Unit, onAdd: (String) -> Unit
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        items.forEach { i ->
            OutlinedCard(modifier = Modifier.padding(16.dp, 4.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(i, paddingSmall)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onDelete(i) }) {
                        Icon(Icons.Sharp.Delete, "Delete")
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            var newExt by remember { mutableStateOf("") }
            OutlinedTextField(
                value = newExt,
                onValueChange = { newExt = it },
                modifier = padding,
                label = { Text("Add new") })
            ElevatedButton(onClick = {
                onAdd(newExt)
                newExt = ""
            }) {
                Icon(Icons.Filled.Add, "Add")
            }
        }
    }
}
