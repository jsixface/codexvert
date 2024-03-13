import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object DesktopMain {
    @JvmStatic
    fun main(args: Array<String>) = application {
        Napier.base(DebugAntilog())
        Window(
            onCloseRequest = ::exitApplication,
            title = "CodeXvert",
            state = WindowState(size = DpSize(1200.dp, 800.dp))
        ) {
            App()
        }
    }
}