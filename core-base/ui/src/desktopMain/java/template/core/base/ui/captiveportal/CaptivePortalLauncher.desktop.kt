/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.ui.captiveportal

import java.awt.Desktop
import java.net.URI

/**
 * Desktop impl: open the system default browser to a known captive-portal probe URL.
 * `http://captive.apple.com/` is the canonical Apple captive-portal probe — most
 * enterprise / hotel / coffee-shop WiFi rewrites this to their sign-in page. If the
 * network has no captive portal, the user sees Apple's plain "Success" page and can
 * close the browser.
 */
actual fun openCaptivePortalSignIn(): Boolean = try {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI("http://captive.apple.com/"))
        true
    } else {
        false
    }
} catch (t: Throwable) {
    false
}
