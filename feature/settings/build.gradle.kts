/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.cmp.feature.convention)
    // Module-local BuildKonfig so the About/version footer can show the fork's app display name
    // WITHOUT a hardcoded string resource (S9/T10 white-label seam). Same mechanism core/network uses.
    alias(libs.plugins.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.coreBase.ui)

            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.settings.generated.resources"
    }
}

// Fork app display name → `kpt.feature.settings.BuildKonfig.APP_DISPLAY_NAME`, read from the fork-owned
// `gradle/fork.properties` (never synced). SettingsScreen's footer renders this instead of a hardcoded
// string resource, so a fork rebrands via fork.properties, not 7 locale strings.xml files (S9/T10).
val settingsForkProps = Properties().apply {
    val f = rootProject.file("gradle/fork.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

buildkonfig {
    packageName = "kpt.feature.settings"
    defaultConfigs {
        buildConfigField(
            STRING,
            "APP_DISPLAY_NAME",
            settingsForkProps.getProperty("app.display.name").orEmpty().ifBlank { "App" },
        )
    }
}
