package ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppPages(val icon: ImageVector, val title: String) {
    HOME(Icons.Filled.Home, "Home"),
    JOBS(Icons.Filled.Inbox, "Jobs"),
    BACKUPS(Icons.Filled.Archive, "Backups"),
    SETTINGS(Icons.Filled.Settings, "Settings"),
}