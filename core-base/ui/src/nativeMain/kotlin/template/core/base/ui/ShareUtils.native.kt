/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
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
        val file = FileKit.openFileSaver(
            suggestedName = title,
            extension = "png",
        )

        file?.write(bytes = image.asSkiaBitmap().readPixels()!!)
    }

    actual suspend fun shareImage(title: String, byte: ByteArray) {
        val file = FileKit.openFileSaver(
            suggestedName = title,
            extension = "png",
        )

        file?.write(bytes = byte)
    }
}
