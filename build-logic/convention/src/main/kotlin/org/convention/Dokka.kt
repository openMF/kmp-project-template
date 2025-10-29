package org.convention

import org.gradle.api.Project

/**
 * Configures the Dokka plugin with automatic moduleName generation based on the project hierarchy.
 */
internal fun Project.configureDokkaConvention() = dokkaGradle {
    val defaultModuleName = project.path
        .trimStart(':')
        .replace(':', '-')
        .ifBlank { project.name }

    println(defaultModuleName)
    if (moduleName.orNull.isNullOrBlank()) {
        moduleName.set(defaultModuleName)
    }
}