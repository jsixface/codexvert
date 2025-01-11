package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ui.home.HomeScreen
import ui.model.AppPages


@Composable
fun MainScreen() {

    Box(modifier = Modifier.fillMaxSize()) {
        var currentPage by rememberSaveable { mutableStateOf(AppPages.HOME) }


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
                AppPages.HOME -> HomeScreen()
                AppPages.JOBS -> JobsScreen()
                AppPages.BACKUPS -> BackupsScreen()
                AppPages.SETTINGS -> SettingsScreen()
            }
        }
    }
}
