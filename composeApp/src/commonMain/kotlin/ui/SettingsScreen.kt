package ui

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ui.model.Screen
import ui.model.ModelState
import viewmodels.SettingsScreenModel

object SettingsScreen : Screen {

    private val padding = Modifier.padding(16.dp)
    private val paddingSmall = Modifier.padding(8.dp)

    override val name: String
        get() = "Settings"

    @Composable
    override fun icon() {
        Icon(Icons.Filled.Settings, contentDescription = name)
    }

    @Composable
    override fun content() {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.width(width = 600.dp).padding(20.dp)
            ) {
                val settingsModel = koinInject<SettingsScreenModel>()
                val scope = rememberCoroutineScope()
                var loading by remember { mutableStateOf(true) }
                var errorLoading by remember { mutableStateOf(false) }
                var workspace by remember { mutableStateOf("") }
                val extensions = remember { mutableStateListOf<String>() }
                val locations = remember { mutableStateListOf<String>() }

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
                                }
                            }
                        }
                    }
                }

                Column(modifier = padding) {
                    Row {
                        Text(
                            text = "Settings",
                            fontSize = 30.sp,
                            modifier = padding,
                            textAlign = TextAlign.Center,
                        )
                        if (loading) {
                            CircularProgressIndicator(modifier = padding.width(30.dp))
                        }
                    }
                    Column {
                        ListEditor("Locations", locations, { locations.remove(it) }, { locations.add(it) })
                        Divider()
                        ListEditor("Extensions", extensions, { extensions.remove(it) }, { extensions.add(it) })
                        Divider()
                        OutlinedTextField(value = workspace,
                            onValueChange = { workspace = it },
                            modifier = padding,
                            label = { Text("Workspace Location") })
                    }
                    Row(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.weight(1f))
                        ElevatedButton(onClick = {
                            scope.launch {
                                settingsModel.save(locations.toList(), extensions.toList(), workspace)
                            }
                        }, modifier = padding) {
                            Text("Save")
                        }
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
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = padding,
                textAlign = TextAlign.Center,
            )
            items.forEach { i ->
                OutlinedCard(
                    modifier = Modifier.padding(16.dp, 4.dp).fillMaxWidth()
                ) {
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
                OutlinedTextField(value = newExt,
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
}
