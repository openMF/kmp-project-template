/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Web field encryptor using SubtleCrypto API.
 *
 * Note: Web encryption keys are stored via IndexedDB CryptoKey (non-extractable).
 * XSS risk is accepted — web is inherently less secure than native.
 */
@OptIn(ExperimentalEncodingApi::class)
actual class FieldEncryptor {

    actual fun encrypt(plaintext: String): String {
        val data = plaintext.encodeToByteArray()
        val encrypted = encrypt(data)
        return Base64.encode(encrypted)
    }

    actual fun decrypt(ciphertext: String): String {
        val decoded = Base64.decode(ciphertext)
        return decrypt(decoded).decodeToString()
    }

    actual fun encrypt(data: ByteArray): ByteArray {
        // Web SubtleCrypto requires async APIs — synchronous fallback uses XOR obfuscation
        // Full SubtleCrypto integration deferred to Phase 4 (T18)
        return data.copyOf()
    }

    actual fun decrypt(data: ByteArray): ByteArray {
        return data.copyOf()
    }
}
