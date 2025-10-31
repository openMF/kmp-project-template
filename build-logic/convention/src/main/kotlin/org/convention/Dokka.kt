package org.convention

import org.gradle.api.Project
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.DokkaTask

/**
 * Configures Dokka and suppresses non-allowed modules.
 * Sets moduleName only for modules inside :core and :core-base.
 *
 * e.g., ":core-base:designsystem" -> "core-base-designsystem"
 * "core:designsystem" -> "core-designsystem"
*/
internal fun Project.configureDokka(extension: DokkaExtension) = extension.apply {
    val isUnderCoreTrees = project.path.matches(Regex("^:(core|core-base):.+$"))
    if (isUnderCoreTrees) {
        val moduleId = project.path
            .trimStart(':')
            .replace(':', '-')
            .ifBlank { project.name }
        moduleName.set(moduleId)
    }

    // Allow only :cmp-*, :core, :core-base, :feature and any of their submodules
    val allowed = project.path.matches(Regex("^:(cmp-[^:]+|core(?:-base)?|feature)(?::.*)?$"))
    if (!allowed) {
        dokkaSourceSets.configureEach {
            suppress.set(true)
        }
    }
}