package org.mifos.core.ui.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.vinceglb.filekit.core.FileKit

actual object ShareUtils {
    actual fun shareText(text: String) {
    }

    actual suspend fun shareImage(title: String, image: ImageBitmap) {
        FileKit.saveFile(
            bytes = image.asSkiaBitmap().readPixels(),
            baseName = "MifosQrCode",
            extension = "png",
        )
    }

    actual suspend fun shareImage(title: String, byte: ByteArray) {
        FileKit.saveFile(
            bytes = byte,
            baseName = "MifosQrCode",
            extension = "png",
        )
    }
}