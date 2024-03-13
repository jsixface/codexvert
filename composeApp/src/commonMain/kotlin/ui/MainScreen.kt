package ui

import Backend
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.home.HomeScreen
import ui.model.Screen


@Composable
fun MainScreen() {

    Surface(modifier = Modifier.fillMaxSize()) {
        var showCloudDialog by remember { mutableStateOf(false) }
        var currentScreen: Screen by remember { mutableStateOf(HomeScreen) }

        Row(modifier = Modifier.fillMaxSize()) {
            if (showCloudDialog) {
                BackendDialog(Backend.host, { showCloudDialog = false }) { Backend.host = it }
            }

            NavigationRail(modifier = Modifier.fillMaxHeight()) {
                listOf(HomeScreen, JobsScreen,BackupsScreen, SettingsScreen).forEach { screen ->
                    NavigationRailItem(icon = { screen.icon() },
                        label = { Text(screen.name) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen })
                }
                Spacer(modifier = Modifier.weight(1f))
                FloatingActionButton(onClick = { showCloudDialog = true }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Filled.Cloud, contentDescription = "Backend")
                }
            }
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                currentScreen.content()
            }
        }
    }
}
