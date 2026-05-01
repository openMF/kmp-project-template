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

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val SERVICE_NAME = "org.mifos.secure"
private const val ACCOUNT_NAME = "field_encryptor_key"

@OptIn(ExperimentalForeignApi::class)
actual class SecureKeyProvider {

    actual fun getKey(): ByteArray? {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to ACCOUNT_NAME,
            kSecReturnData to true,
        )

        val result = kotlinx.cinterop.memScoped {
            val ref = kotlinx.cinterop.alloc<kotlinx.cinterop.ObjCObjectVar<Any?>>()
            val status = SecItemCopyMatching(query as kotlinx.cinterop.CValuesRef<*>, ref.ptr)
            if (status == errSecSuccess) ref.value as? NSData else null
        }

        return result?.let { nsData ->
            ByteArray(nsData.length.toInt()).also { bytes ->
                bytes.usePinned { pinned ->
                    platform.posix.memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                }
            }
        }
    }

    actual fun generateKey(): ByteArray {
        deleteKey()
        val key = SecureRandom().nextBytes(32)
        val nsData = key.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = key.size.toULong())
        }
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to ACCOUNT_NAME,
            kSecValueData to nsData,
        )
        val status = SecItemAdd(query as kotlinx.cinterop.CValuesRef<*>, null)
        if (status != errSecSuccess) {
            throw SecurityException("Failed to store key in Keychain: $status")
        }
        return key
    }

    actual fun deleteKey() {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to ACCOUNT_NAME,
        )
        SecItemDelete(query as kotlinx.cinterop.CValuesRef<*>)
    }
}
