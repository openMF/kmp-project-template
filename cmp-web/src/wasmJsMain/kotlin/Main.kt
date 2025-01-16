import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import cmp.shared.SharedApp
import cmp.shared.di.initKoin
import org.jetbrains.compose.resources.configureWebResources

/**
 * Main function.
 * This function is used to start the application.
 * @see CanvasBasedWindow
 * @see SharedApp
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()

    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }

    CanvasBasedWindow(
        title = "WebApp",
        canvasElementId = "ComposeTarget",
    ) {
        SharedApp()
    }
}