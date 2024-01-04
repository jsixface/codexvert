package ui

import Backend
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                listOf(HomeScreen, JobsScreen, SettingsScreen).forEach { screen ->
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
