/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package local

import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.gradle.api.Project

/**
 * Fork-owned hook for extra product-flavor dimensions.
 *
 * Ships EMPTY with a worked example in comments. It is `owner: fork` so a template sync never overwrites it — which is
 * exactly why the template cannot put anything real here.
 */
object LocalFlavors {
    /**
     * Adds this fork's flavor dimensions to [ext].
     *
     * Called by the flavors convention plugin after the template's own dimensions, so a fork adds to them rather than
     * replacing them.
     *
     * @param ext the flavor extension to configure.
     * @param project the project being configured, for reading properties.
     */
    @JvmStatic
    fun apply(ext: KmpFlavorExtension, project: Project) {
        // ── Example: add a second "bank" dimension with two flavors ─────────────
        //
        // Uncomment this entire block and rename "bank" / "bankA" / "bankB" to
        // your own dimension and flavor names. Fill in real values before building.
        //
        // ext.flavorDimensions.register("bank") { priority.set(1) }
        //
        // ext.flavors.register("bankA") {
        //     dimension.set("bank")
        //     isDefault.set(true)                          // default when no -PkmpFlavorBank flag
        //     applicationIdSuffix.set(".banka")            // Android applicationId + macOS bundle ID
        //     bundleIdSuffix.set(".banka")                 // iOS xcconfig PRODUCT_BUNDLE_IDENTIFIER
        //     desktopWindowTitleSuffix.set(" (Bank A)")    // dock name, package name, Windows menu
        //     webTitleSuffix.set(" (Bank A)")              // HTML <title> and OG tags
        //     buildConfigField("String", "BANK_URL", "\"https://api.banka.com\"")
        //     buildConfigField("String", "BANK_NAME", "\"Bank A\"")
        // }
        //
        // ext.flavors.register("bankB") {
        //     dimension.set("bank")
        //     applicationIdSuffix.set(".bankb")
        //     bundleIdSuffix.set(".bankb")
        //     desktopWindowTitleSuffix.set(" (Bank B)")
        //     webTitleSuffix.set(" (Bank B)")
        //     buildConfigField("String", "BANK_URL", "\"https://api.bankb.com\"")
        //     buildConfigField("String", "BANK_NAME", "\"Bank B\"")
        // }
        // ────────────────────────────────────────────────────────────────────────
    }
}
