import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import org.jetbrains.compose.resources.configureWebResources
import org.mifos.shared.MifosPaySharedApp
import org.mifos.shared.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()

    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }

    CanvasBasedWindow(
        title = "MifosAppTemplate",
        canvasElementId = "ComposeTarget",
    ) {
        MifosPaySharedApp()
    }
}