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

import kotlinx.browser.window

/**
 * JS / WasmJs impl (shared via jsCommonMain): open the system default browser to a known
 * captive-portal probe URL in a new tab. `http://captive.apple.com/` is the canonical
 * Apple captive-portal probe — most enterprise / hotel / coffee-shop WiFi rewrites this
 * to their sign-in page.
 */
actual fun openCaptivePortalSignIn(): Boolean = try {
    window.open("http://captive.apple.com/", "_blank")
    true
} catch (t: Throwable) {
    false
}
