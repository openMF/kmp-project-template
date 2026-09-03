/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// feature-deps.gradle.kts — the FORK-OWNED feature-dependency seam (white-label, S7/F4).
//
// `cmp-navigation/build.gradle.kts` (template-owned) applies this script via `apply(from = ...)`. A fork
// adds/removes a feature module dependency by editing ONLY this file — it never touches the template build
// file, so a template sync full-copies `cmp-navigation/build.gradle.kts` while these deps survive. This is
// the build-graph twin of the `cmp-navigation/registry` runtime seams (FeatureRegistry etc.); adding a
// feature is: include it in `settings.gradle.kts`, add its dep here, and register its module + routes in
// `FeatureRegistry`.
//
// String `project(":feature:x")` notation (not the type-safe `projects.feature.x` accessors) is used
// deliberately: type-safe project accessors are NOT generated for `apply(from = ...)` script plugins. The
// `commonMainImplementation` configuration is the KMP-created implementation configuration for the
// commonMain source set; it exists by the time this script is applied (after the `kotlin { }` block).

dependencies {
    // demo:begin — default demo feature set. customizer --clean empties this block for a clean fork;
    // a fork replaces these with its own `"commonMainImplementation"(project(":feature:<name>"))` lines.
    "commonMainImplementation"(project(":feature:currency-rates"))
    "commonMainImplementation"(project(":feature:emi-calculator"))
    "commonMainImplementation"(project(":feature:bills"))
    "commonMainImplementation"(project(":feature:loans"))
    "commonMainImplementation"(project(":feature:amortization"))
    "commonMainImplementation"(project(":feature:rates"))
    "commonMainImplementation"(project(":feature:calculators"))
    "commonMainImplementation"(project(":feature:macro"))
    "commonMainImplementation"(project(":feature:crypto"))
    "commonMainImplementation"(project(":feature:showcase"))
    "commonMainImplementation"(project(":feature:alerts"))
    "commonMainImplementation"(project(":feature:watchlist"))
    "commonMainImplementation"(project(":feature:add-to-watchlist"))
    "commonMainImplementation"(project(":feature:cloudtodo"))
    // demo:end
}
