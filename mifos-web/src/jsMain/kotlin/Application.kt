import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import org.mifos.shared.MifosSharedApp
import org.mifos.shared.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()

    onWasmReady {
        ComposeViewport(document.body!!) {
            MifosSharedApp()
        }
    }
}