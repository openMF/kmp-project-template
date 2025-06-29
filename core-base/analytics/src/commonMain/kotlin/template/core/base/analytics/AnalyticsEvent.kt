/*
 * Copyright 2023 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.analytics

/**
 * Represents an analytics event.
 *
 * @param type - the event type. Wherever possible use one of the standard
 *    event `Types`, however, if there is no suitable event type already
 *    defined, a custom event can be defined as long as it is configured in
 *    your backend analytics system (for example, by creating a Firebase
 *    Analytics custom event).
 * @param extras - list of parameters which supply additional context to
 *    the event. See `Param`.
 */
data class AnalyticsEvent(
    val type: String,
    val extras: List<Param> = emptyList(),
)

object Types {
    const val SCREEN_VIEW = "screen_view"
}

data class Param(val key: String, val value: String)

object ParamKeys {
    const val SCREEN_NAME = "screen_name"
}
