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
 * Formats a day of the month into a 2-digit string.
 * Example: "1" becomes "01", "10" stays "10".
 *
 * @return The formatted day as a 2-digit string.
 */
fun String.formatDay(): String = this.padStart(2, '0')
