import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import services.Koin
import ui.theme.AppTheme
import ui.MainScreen

@Composable
fun App() {
    KoinApplication(application = {
        modules(Koin.services)
    }) {
        AppTheme {
            MainScreen()
        }
    }
}