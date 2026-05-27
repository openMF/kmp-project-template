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

/**
 * Opens the platform-native captive-portal sign-in flow when the user is on
 * a WiFi network requiring sign-in (e.g. hotel WiFi, coffee shop).
 *
 * Per-platform behaviour:
 *  - **Android** — opens `Settings.ACTION_WIFI_SETTINGS`. Requires a one-time call to
 *    [installCaptivePortalLauncher] from the `Application.onCreate()` so the launcher
 *    holds an application Context.
 *  - **iOS** — opens the URL scheme `prefs:root=WIFI` (or `App-Prefs:root=WIFI`). Apps
 *    must declare `prefs` in `LSApplicationQueriesSchemes` in `Info.plist` for this
 *    to succeed; otherwise the call returns `false`.
 *  - **Desktop** — opens the system browser to `http://captive.apple.com/`, a known
 *    captive-portal probe URL that most enterprise/hotel WiFi networks will rewrite
 *    to their sign-in page.
 *  - **JS / WasmJs** — opens `http://captive.apple.com/` in a new browser tab.
 *
 * @return `true` if the platform dispatched the launch; `false` if it could not (e.g. the
 *   Android launcher was never installed, iOS lacks the URL scheme allowlist, or the
 *   desktop has no browser).
 */
expect fun openCaptivePortalSignIn(): Boolean
