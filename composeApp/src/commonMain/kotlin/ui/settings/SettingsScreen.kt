package ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.jsixface.common.AutoConversion
import io.github.jsixface.common.Settings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ui.model.ModelState

@Composable
fun SettingsScreen() {
    val settingsModel = koinInject<SettingsScreenModel>()
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var errorLoading by remember { mutableStateOf(false) }
    var workspace by remember { mutableStateOf("") }
    val extensions = remember { mutableStateListOf<String>() }
    val locations = remember { mutableStateListOf<String>() }
    var autoConversion by remember { mutableStateOf(AutoConversion()) }
    var takeBackups by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
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
                    takeBackups = settings.takeBackups
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Column(
            modifier = Modifier
                .width(width = 800.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium,
                )
                if (loading) {
                    Spacer(Modifier.width(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            // File Scope Section
            SettingsSection(
                title = "File Scope",
                subtitle = "Where to scan and what to include"
            ) {
                ListEditor(
                    "Locations",
                    locations,
                    { locations.remove(it) },
                    { if (it.isNotBlank()) locations.add(it) })
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Extensions Section
                SettingsSection(
                    title = "Extensions",
                    subtitle = "File Extensions to look for",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    ExtensionEditor(
                        extensions,
                        { extensions.remove(it) },
                        { if (it.isNotBlank()) extensions.add(it) })
                }

                // Auto Conversion Section
                SettingsSection(
                    title = "Auto Conversion",
                    subtitle = "Automatically convert media formats",
                    modifier = Modifier.weight(1.2f).fillMaxHeight()
                ) {
                    AutoConvertSettings(autoConversion) { autoConversion = it }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Workspace & Safety Section
            SettingsSection(
                title = "Workspace & Safety",
                subtitle = ""
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Workspace", style = MaterialTheme.typography.titleSmall)
                        OutlinedTextField(
                            value = workspace,
                            onValueChange = { workspace = it },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    Icons.Rounded.FolderOpen,
                                    contentDescription = "Select Folder"
                                )
                            }
                        )
                    }

                    Column {
                        Text("Backups", style = MaterialTheme.typography.titleSmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(checked = takeBackups, onCheckedChange = { takeBackups = it })
                            Text("Keep original files", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(100.dp)) // Space for the floating button
        }

        Button(
            onClick = {
                scope.launch {
                    settingsModel.save(
                        Settings(
                            libraryLocations = locations,
                            workspaceLocation = workspace,
                            videoExtensions = extensions,
                            autoConversion = autoConversion,
                            takeBackups = takeBackups
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF135D43))
        ) {
            Text("Save Changes", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            content()
        }
    }
}

@Composable
fun ListEditor(
    title: String, items: List<String>, onDelete: (String) -> Unit, onAdd: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        items.forEach { i ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(i, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onDelete(i) }) {
                        Icon(Icons.Rounded.Delete, "Delete", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            var newItem by remember { mutableStateOf("") }
            OutlinedTextField(
                value = newItem,
                onValueChange = { newItem = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add location") },
                leadingIcon = { Icon(Icons.Default.Add, null) },
                singleLine = true
            )
            Button(
                onClick = {
                    onAdd(newItem)
                    newItem = ""
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
fun ExtensionEditor(
    items: List<String>, onDelete: (String) -> Unit, onAdd: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Extensions", style = MaterialTheme.typography.titleSmall)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { ext ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(ext) },
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp).clickable { onDelete(ext) }
                        )
                    }
                )
            }
        }
        var newItem by remember { mutableStateOf("") }
        OutlinedTextField(
            value = newItem,
            onValueChange = { newItem = it },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            placeholder = { Text("Add extension") },
            leadingIcon = { Icon(Icons.Default.Add, null) },
            singleLine = true,
            trailingIcon = {
                if (newItem.isNotBlank()) {
                    IconButton(onClick = {
                        onAdd(newItem)
                        newItem = ""
                    }) {
                        Icon(Icons.Default.Add, "Add")
                    }
                }
            }
        )
    }
}
