package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import ui.model.ModelState
import ui.model.Screen
import viewmodels.BackupScreenViewModel

object BackupsScreen : Screen {

    private val padding = Modifier.padding(16.dp)
    private val paddingSmall = Modifier.padding(8.dp)
    private val filePattern = Regex(".*\\.([0-9]+)\\.bkp$")

    override val name: String
        get() = "Backups"

    @Composable
    override fun icon() {
        Icon(Icons.Filled.Archive, contentDescription = name)
    }

    @Composable
    override fun content() {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val backupScreenViewModel = koinInject<BackupScreenViewModel>()
            val scope = rememberCoroutineScope()
            var backups by remember { mutableStateOf(listOf<String>()) }
            suspend fun loadBackups() {
                backupScreenViewModel.backupFiles.collect {
                    if (it is ModelState.Success) {
                        backups = it.result
                    }
                }
            }
            LaunchedEffect(Unit) {
                scope.launch {
                    loadBackups()
                }
            }
            BackupContent(
                backups,
                onClear = {
                    scope.launch {
                        backupScreenViewModel.clearBackups()
                        loadBackups()
                    }
                },
                onDelete = {
                    scope.launch {
                        backupScreenViewModel.delete(it)
                        loadBackups()
                    }
                })

        }

    }

    @Composable
    fun BackupContent(backups: List<String>, onDelete: (String) -> Unit, onClear: () -> Unit) {
        Card(
            modifier = Modifier.width(width = 900.dp).fillMaxHeight().padding(20.dp)
        ) {
            Column(modifier = padding) {
                Row {
                    Text(
                        text = "Backups",
                        fontSize = 30.sp,
                        modifier = padding,
                        textAlign = TextAlign.Center,
                    )
                }
                Column {
                    backups.forEach { backup -> BackupItem(backup) { onDelete(backup) } }
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    ElevatedButton(onClick = onClear, modifier = padding) {
                        Text("Clear All Backups")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    @Composable
    fun BackupItem(backup: String, onDelete: () -> Unit) {
        Column {
            OutlinedCard(
                modifier = Modifier.padding(16.dp, 4.dp).fillMaxWidth()
            ) {
                val ts = filePattern.find(backup)?.groupValues?.get(1)?.toLongOrNull() ?: 0
                val modified = Instant.fromEpochSeconds(ts).toLocalDateTime(TimeZone.currentSystemDefault())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                        Text(backup.substringAfterLast('/'), modifier = paddingSmall)
                        Text(
                            "Completed at ${modified.date} ${modified.time}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = paddingSmall
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Sharp.Delete, "Delete Backup")
                    }
                }
            }
        }
    }
}
