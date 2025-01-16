import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import cmp.shared.SharedApp
import cmp.shared.di.initKoin

/**
 * Main function.
 * This function is used to start the application.
 * @see ComposeViewport
 * @see SharedApp
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()

    onWasmReady {
        ComposeViewport(document.body!!) {
            SharedApp()
        }
    }
}