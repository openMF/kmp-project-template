/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.demo.profile

/**
 * What the profile screen displays.
 *
 * In the template this carries only the app's display name — the demo profile is a local,
 * signed-out placeholder. It is a real domain model rather than a raw `String` because a fork
 * replaces the *fetcher*, not the screen: swapping in a signed-in user means widening this
 * model and returning it from the store, with the ViewModel and Composable untouched.
 */
data class ProfileInfo(
    val appDisplayName: String,
)
