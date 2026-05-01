/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.datastore

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

actual class SecureSettingsFactory {
    actual fun create(): Settings {
        return KeychainSettings(serviceName = "org.mifos.secure")
    }
}
