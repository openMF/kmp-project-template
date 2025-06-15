/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.feature.home.extensions

/**
 * Extension function that formats an integer to a two-digit clock pattern.
 * If the integer is less than 10, a leading zero is added. Otherwise, the number is returned as is.
 *
 * @return A string representation of the integer formatted as a two-digit number.
 */
fun Int.toClockPattern(): String {
    return if (this < 10) "0$this" else this.toString()
}
