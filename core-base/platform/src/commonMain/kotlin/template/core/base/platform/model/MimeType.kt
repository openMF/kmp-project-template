/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.platform.model

enum class MimeType(val value: String, vararg val extensions: String) {
    // Images
    IMAGE_JPEG("image/jpeg", "jpg", "jpeg"),
    IMAGE_PNG("image/png", "png"),
    IMAGE_GIF("image/gif", "gif"),
    IMAGE_WEBP("image/webp", "webp"),
    IMAGE_BMP("image/bmp", "bmp"),
    IMAGE_SVG("image/svg+xml", "svg"),

    // Videos
    VIDEO_MP4("video/mp4", "mp4"),
    VIDEO_WEBM("video/webm", "webm"),
    VIDEO_MKV("video/x-matroska", "mkv"),
    VIDEO_AVI("video/x-msvideo", "avi"),
    VIDEO_MOV("video/quicktime", "mov"),

    // Audio
    AUDIO_MPEG("audio/mpeg", "mp3"),
    AUDIO_WAV("audio/wav", "wav"),
    AUDIO_OGG("audio/ogg", "ogg"),
    AUDIO_M4A("audio/mp4", "m4a"),
    AUDIO_FLAC("audio/flac", "flac"),

    // Documents
    APPLICATION_PDF("application/pdf", "pdf"),
    APPLICATION_DOC("application/msword", "doc"),
    APPLICATION_DOCX(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "docx",
    ),
    APPLICATION_XLS("application/vnd.ms-excel", "xls"),
    APPLICATION_XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    APPLICATION_PPT("application/vnd.ms-powerpoint", "ppt"),
    APPLICATION_PPTX(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "pptx",
    ),
    TEXT_PLAIN("text/plain", "txt"),

    // Archives
    APPLICATION_ZIP("application/zip", "zip"),
    APPLICATION_RAR("application/x-rar-compressed", "rar"),
    APPLICATION_7Z("application/x-7z-compressed", "7z"),

    // Default for unknown types
    UNKNOWN("application/octet-stream"),
    ;

    companion object {
        // Map to store file extensions and their corresponding MimeType
        private val extensionToMimeType = mutableMapOf<String, MimeType>()

        init {
            // Populate the map with extensions and their corresponding MimeType
            entries.forEach { mimeType ->
                mimeType.extensions.forEach { extension ->
                    extensionToMimeType[extension] = mimeType
                }
            }
        }

        /**
         * Returns the MimeType corresponding to the given file extension.
         * If the extension is not found, returns UNKNOWN.
         */
        fun fromExtension(extension: String): MimeType = extensionToMimeType[extension.lowercase()] ?: UNKNOWN

        /**
         * Returns the MimeType corresponding to the given file name.
         * If the file name has no extension or the extension is not found, returns UNKNOWN.
         */
        fun fromFileName(fileName: String): MimeType {
            val extension = fileName.substringAfterLast(
                delimiter = '.',
                missingDelimiterValue = "",
            ).lowercase()
            return fromExtension(extension)
        }
    }
}
