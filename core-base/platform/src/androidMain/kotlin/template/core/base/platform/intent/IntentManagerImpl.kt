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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import template.core.base.platform.model.MimeType
import template.core.base.platform.utils.isBuildVersionBelow

class IntentManagerImpl(
    private val context: Context,
) : IntentManager {
    override fun startActivity(intent: Any) {
        try {
            Log.d("IntentManagerImpl", "Starting activity: $intent")
            context.startActivity(intent as Intent)
        } catch (_: ActivityNotFoundException) {
            // no-op
        }
    }

    override fun launchUri(uri: String) {
        Log.d("IntentManagerImpl", "Launching URI: $uri")
        val androidUri = uri.toUri()
        if (androidUri.scheme.equals(other = "androidapp", ignoreCase = true)) {
            val packageName = androidUri.toString().removePrefix(prefix = "androidapp://")
            if (isBuildVersionBelow(Build.VERSION_CODES.TIRAMISU)) {
                startActivity(createPlayStoreIntent(packageName))
            } else {
                try {
                    context
                        .packageManager
                        .getLaunchIntentSenderForPackage(packageName)
                        .sendIntent(context, Activity.RESULT_OK, null, null, null)
                } catch (_: IntentSender.SendIntentException) {
                    startActivity(createPlayStoreIntent(packageName))
                }
            }
        } else {
            val newUri = if (androidUri.scheme == null) {
                androidUri.buildUpon().scheme("https").build()
            } else {
                androidUri.normalizeScheme()
            }
            startActivity(Intent(Intent.ACTION_VIEW, newUri))
        }
    }

    override fun shareText(text: String) {
        Log.d("IntentManagerImpl", "Sharing text: $text")
        val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    override fun shareFile(fileUri: String, mimeType: MimeType) {
        Log.d("IntentManagerImpl", "Sharing file: $fileUri with mime type: $mimeType")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            // Add file URI directly
            putExtra(Intent.EXTRA_STREAM, fileUri.toUri())

            // Add promotional text
            putExtra(
                Intent.EXTRA_TEXT,
                "*Downloaded using our awesome app!* \n\n" +
                    "*Download now from the Play Store!* \n" +
                    "https://play.google.com/store/apps/details?id=${context.packageName}",
            )

            // Grant read permission to receiving app
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = mimeType.value
        }

        // Create chooser to let user pick sharing app
        val chooserIntent = Intent.createChooser(
            shareIntent,
            "Share your media",
        )

        startActivity(chooserIntent)
    }

    @Suppress("ReturnCount")
    override fun getShareDataFromIntent(intent: Any): IntentManager.ShareData? {
        intent as Intent

        if (intent.action != Intent.ACTION_SEND) return null
        return if (intent.type?.contains("text/") == true) {
            val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            val title = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
            IntentManager.ShareData.TextSend(
                subject = subject,
                text = title,
            )
        } else {
            null
        }
    }

    override fun createDocumentIntent(fileName: String): Any {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Attempt to get the MIME type from the file extension
            val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
            type = extension?.let {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
            } ?: "*/*"

            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
    }

    override fun startApplicationDetailsSettingsActivity() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = ("package:" + context.packageName).toUri()
        startActivity(intent = intent)
    }

    override fun startDefaultEmailApplication() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_EMAIL)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun createPlayStoreIntent(packageName: String): Intent {
        val playStoreUri = "https://play.google.com/store/apps/details"
            .toUri()
            .buildUpon()
            .appendQueryParameter("id", packageName)
            .build()
        return Intent(Intent.ACTION_VIEW, playStoreUri)
    }
}
