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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mobilebytelabs.kmptoolkit.intentlauncher.ExperimentalIntentLauncherApi
import com.mobilebytelabs.kmptoolkit.intentlauncher.IntentResult
import com.mobilebytelabs.kmptoolkit.intentlauncher.rememberIntentLauncher
import kotlinx.coroutines.launch

/**
 * Returns a stable `() -> Unit` lambda that opens the platform-native captive-portal
 * sign-in flow using [cmp-intent-launcher](https://github.com/MobileByteLabs/KmpToolkit).
 *
 * Per-platform behaviour:
 *  - **Android** — fires `android.settings.WIFI_SETTINGS` via `ActivityResultContracts`;
 *    no application-context wiring required (the Compose activity owns the launcher).
 *  - **iOS** — defers to `onUnsupported`; URL-scheme support (`prefs:root=WIFI`) is
 *    planned for a future KmpToolkit release.
 *  - **Desktop / Web** — defers to `onUnsupported`; captive portals rarely apply to
 *    these platforms in practice.
 *
 * The returned lambda is safe to call from any button `onClick` — it dispatches the
 * intent on the calling coroutine scope and is automatically stable across recompositions.
 */
@OptIn(ExperimentalIntentLauncherApi::class)
@Composable
fun rememberOpenCaptivePortalSignIn(): () -> Unit {
    val launcher = rememberIntentLauncher()
    val scope = rememberCoroutineScope()
    return remember(launcher, scope) {
        {
            scope.launch {
                launcher.launch {
                    action("android.settings.WIFI_SETTINGS")
                    onUnsupported { IntentResult.Ok(null) }
                }
            }
        }
    }
}
