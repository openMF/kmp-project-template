package org.mifos.core.ui.utils

import androidx.compose.ui.graphics.ImageBitmap
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual object ShareUtils {
    actual fun shareText(text: String) {
        val currentViewController = UIApplication.sharedApplication().keyWindow?.rootViewController
        val activityViewController = UIActivityViewController(listOf(text), null)
        currentViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null,
        )
    }

    actual suspend fun shareImage(title: String, image: ImageBitmap) {
    }

    actual suspend fun shareImage(title: String, byte: ByteArray) {
    }
}
