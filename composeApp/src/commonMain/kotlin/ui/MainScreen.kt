package ui

import Backend
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ui.home.HomeScreen
import ui.model.AppPages


@Composable
fun MainScreen() {

    Surface(modifier = Modifier.fillMaxSize()) {
        var showCloudDialog by remember { mutableStateOf(false) }
        var currentPage by rememberSaveable { mutableStateOf(AppPages.HOME) }

        if (showCloudDialog) {
            BackendDialog(Backend.host, { showCloudDialog = false }) { Backend.host = it }
        }

        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppPages.entries.forEach { page ->
                    item(
                        icon = { Icon(page.icon, contentDescription = page.title) },
                        onClick = { currentPage = page },
                        label = { Text(page.title) },
                        selected = currentPage == page
                    )
                }
            }
        ) {
            when (currentPage) {
                AppPages.HOME -> HomeScreen.content()
                AppPages.JOBS -> JobsScreen()
                AppPages.BACKUPS -> BackupsScreen()
                AppPages.SETTINGS -> SettingsScreen()
            }
        }
//        FloatingActionButton(onClick = { showCloudDialog = true }, modifier = Modifier.padding(8.dp)) {
//            Icon(Icons.Filled.Cloud, contentDescription = "Backend")
//        }
    }
}
