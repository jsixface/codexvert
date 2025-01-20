package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.jsixface.common.CodecsCollection
import org.koin.compose.koinInject
import ui.home.HomeScreen
import ui.model.AppPages
import ui.model.ModelState
import viewmodels.AppViewModel


@Composable
fun MainScreen() {

    Box(modifier = Modifier.fillMaxSize()) {
        var currentPage by rememberSaveable { mutableStateOf(AppPages.HOME) }
        var codecsCollection by remember { mutableStateOf<CodecsCollection?>(null) }
        val viewModel = koinInject<AppViewModel>()
        var errorLoading by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.codecsCollection.collect {
                when (it) {
                    is ModelState.Init -> {
                        errorLoading = false
                    }

                    is ModelState.Error -> {
                        errorLoading = true
                    }

                    is ModelState.Success -> {
                        errorLoading = false
                        codecsCollection = it.result
                    }
                }
            }
        }
        if (errorLoading) {
            Text("Error Loading!!!", style = MaterialTheme.typography.headlineSmall)
        } else {
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
                    AppPages.HOME -> HomeScreen(codecsCollection)
                    AppPages.JOBS -> JobsScreen()
                    AppPages.BACKUPS -> BackupsScreen()
                    AppPages.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}
