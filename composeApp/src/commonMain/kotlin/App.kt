import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import services.Koin
import ui.MainScreen
import ui.theme.AppTheme

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