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

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Application context held weakly across the launcher's lifetime. Wired by
 * [installCaptivePortalLauncher] (typically called from `Application.onCreate()`).
 *
 * Holding the **application** context here is safe — it lives for the entire process and
 * never leaks an Activity. If [openCaptivePortalSignIn] is called before
 * [installCaptivePortalLauncher], the launcher returns `false` and logs nothing.
 */
private var appContext: Context? = null

/**
 * One-time installer. Call exactly once from your Android [android.app.Application]'s
 * `onCreate()`:
 *
 * ```kotlin
 * override fun onCreate() {
 *     super.onCreate()
 *     installCaptivePortalLauncher(this)
 *     // ... rest of init ...
 * }
 * ```
 */
fun installCaptivePortalLauncher(context: Context) {
    appContext = context.applicationContext
}

/**
 * Opens Android Settings → Wi-Fi. From there the user can tap the active WiFi network
 * and the system surfaces the captive-portal sign-in if one was detected (API 26+).
 *
 * On API 26+ Android also surfaces a notification with a direct sign-in link when a
 * captive portal is detected; this method is the manual-fallback path users can take
 * from inside our app.
 */
actual fun openCaptivePortalSignIn(): Boolean {
    val ctx = appContext ?: return false
    return try {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
        true
    } catch (t: Throwable) {
        false
    }
}
