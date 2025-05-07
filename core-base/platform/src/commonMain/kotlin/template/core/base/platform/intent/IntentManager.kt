/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform.intent

import template.core.base.platform.model.MimeType

interface IntentManager {
    fun startActivity(intent: Any)
    fun launchUri(uri: String)
    fun shareText(text: String)
    fun shareFile(fileUri: String, mimeType: MimeType)
    fun getShareDataFromIntent(intent: Any): ShareData?
    fun createDocumentIntent(fileName: String): Any
    fun startApplicationDetailsSettingsActivity()
    fun startDefaultEmailApplication()

    sealed class ShareData {
        data class TextSend(
            val subject: String?,
            val text: String,
        ) : ShareData()
    }
}
