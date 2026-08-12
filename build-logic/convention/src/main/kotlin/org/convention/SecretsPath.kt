/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.convention

import org.gradle.api.Project
import java.io.File

/**
 * Resolve a secret's on-disk path by DELEGATING to the ONE resolver — `deployment/scripts/build-secrets`
 * (the bash CLI over `deployment/_shared/lib/build_secrets.rb`, which reads `secrets/LAYOUT.yaml`).
 *
 * This is NOT a parallel resolver (RULE-SECRETS-LAYOUT-001 / check-secrets-resolver.sh SR-7/SR-12 forbid
 * those): the Gradle build never reads `secrets/LAYOUT.yaml` itself and never hardcodes a `secrets/live/…`
 * path — it shells out to `build-secrets path <key>`, which composes `roots.{live,sample}` + the secret's
 * `rel:` (live-wins-else-sample). So the whole `secrets/live/<platform>` tree (android / apple / desktop /
 * web / supabase) is re-manageable by editing `secrets/LAYOUT.yaml` alone, and Gradle + Fastlane share ONE
 * resolver. Returns an absolute [File] (the CLI prints a repo-root-relative path).
 */
fun Project.resolveSecretPath(key: String): File {
    val repoRoot = rootProject.projectDir
    val cli = File(repoRoot, "deployment/scripts/build-secrets")
    // `build-secrets` is a `#!/usr/bin/env bash` CLI. Windows cannot start a shebang script directly
    // (it is not a .exe/.bat/.cmd) — and this resolver runs at `:cmp-android` configuration time, so a
    // raw exec breaks EVERY Gradle build on Windows (desktop/web included), not just Android signing.
    // Launch it through bash (Git Bash is on the GitHub windows-latest PATH) with a forward-slashed
    // path so it is not mangled; other OSes exec it directly. Both hit the same one resolver.
    val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
    val command = if (isWindows) {
        listOf("bash", cli.absolutePath.replace('\\', '/'), "path", key)
    } else {
        listOf(cli.absolutePath, "path", key)
    }
    val rel = providers.exec {
        workingDir = repoRoot
        commandLine(command)
    }.standardOutput.asText.get().trim()
    return File(repoRoot, rel)
}
