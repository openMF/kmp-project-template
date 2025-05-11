/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import org.jetbrains.compose.compose

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.kmp.library.convention)
}

android {
    namespace = "org.mifos.corebase.database"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.room.runtime)
        }

        desktopMain.dependencies {
            implementation(libs.androidx.room.runtime)
        }

        nativeMain.dependencies {
            implementation(libs.androidx.room.runtime)
        }
    }
}
