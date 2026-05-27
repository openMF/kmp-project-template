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

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS / macOS impl: open the system Settings app at the WiFi pane via the `prefs:` URL
 * scheme. App must declare `prefs` in `LSApplicationQueriesSchemes` in `Info.plist`
 * (otherwise `canOpenURL` returns false and we fall through to `false`).
 *
 * If the primary scheme `prefs:root=WIFI` fails, we try the older `App-Prefs:root=WIFI`
 * variant before giving up. On macOS the URL scheme is silently ignored — the call
 * returns `false` and the caller's `onRetry` path is shown instead.
 */
actual fun openCaptivePortalSignIn(): Boolean {
    val app = UIApplication.sharedApplication
    val candidates = listOf("prefs:root=WIFI", "App-Prefs:root=WIFI")
    val opened = candidates.firstNotNullOfOrNull { scheme ->
        NSURL.URLWithString(scheme)?.takeIf { app.canOpenURL(it) }
    }
    return if (opened != null) {
        app.openURL(opened)
        true
    } else {
        false
    }
}
