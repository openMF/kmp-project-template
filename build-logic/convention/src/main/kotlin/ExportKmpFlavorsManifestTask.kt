/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Exports the currently declared `kmpFlavors {}` DSL as a machine-readable
 * `variants.json` manifest — the **single derivation point** consumed by
 * every downstream deployment stage (fastlane `variant_resolver.rb`,
 * `.github/workflows/*.yml` `resolve-variants` job, and the
 * `openMF/mifos-x-actionhub` reusable workflow).
 *
 * ## Single source of truth chain
 *
 *   KMPFlavorsConventionPlugin (kmpFlavors {} DSL)
 *     └─ this task reads flavors + buildTypes at execution time
 *           └─ writes build/kmp-flavors/variants.json
 *                 └─ variant_resolver.rb reads the manifest at CI time
 *                       └─ derives gradle task / artifact path / iOS scheme by convention
 *                             └─ delegates all secret paths to BuildSecrets.for(flavor:)
 *
 * ## Consumer-side, zero plugin change (D8 anchor)
 *
 * Reads only the plugin's PUBLIC `KmpFlavorExtension` surface
 * (`flavors: NamedDomainObjectContainer<FlavorConfig>` +
 * `buildTypes: NamedDomainObjectContainer<BuildTypeConfig>`) via lazy
 * `MapProperty` providers wired in [KMPFlavorsConventionPlugin]. No file
 * under `build-logic/flavor-plugin/**` is touched by this task — the
 * kmp-product-flavors plugin remains untouched (epic decision D8).
 *
 * ## Fully dynamic (D9 anchor)
 *
 * The manifest is regenerated from the DSL on every run. Adding, renaming,
 * or removing a flavor / build-type in the DSL automatically propagates to
 * every CI stage on the next `resolve-variants` invocation — no committed
 * derived list, no hand-maintained flavor catalog, no drift surface.
 *
 * ## Config-cache safety
 *
 * All inputs are declared through `MapProperty<String, String>` +
 * `RegularFileProperty` — no `Project` reference is captured in the task
 * action. The provider blocks that resolve the DSL live in the registration
 * site ([KMPFlavorsConventionPlugin.registerExportKmpFlavorsManifestTask])
 * and materialize scalar maps that the task action consumes.
 *
 * ## Output schema (`variants.json`)
 *
 * ```json
 * {
 *   "schema_version": 1,
 *   "flavors": [
 *     { "name": "demo", "applicationIdSuffix": ".demo", "bundleIdSuffix": ".demo" },
 *     { "name": "prod", "applicationIdSuffix": "",      "bundleIdSuffix": ""      }
 *   ],
 *   "buildTypes": [
 *     { "name": "debug",   "applicationIdSuffix": ".debug"   },
 *     { "name": "release", "applicationIdSuffix": ""         },
 *     { "name": "staging", "applicationIdSuffix": ".staging" }
 *   ],
 *   "variants": [
 *     { "name": "demoDebug",   "flavor": "demo", "buildType": "debug"   },
 *     { "name": "demoRelease", "flavor": "demo", "buildType": "release" },
 *     { "name": "demoStaging", "flavor": "demo", "buildType": "staging" },
 *     { "name": "prodDebug",   "flavor": "prod", "buildType": "debug"   },
 *     { "name": "prodRelease", "flavor": "prod", "buildType": "release" },
 *     { "name": "prodStaging", "flavor": "prod", "buildType": "staging" }
 *   ]
 * }
 * ```
 *
 * Extended per-variant metadata (`gradle_task`, `artifact_path`, `ios_scheme`) is
 * derived **by convention** in the resolver (epic decision D9) and is deliberately
 * NOT stored in the manifest — the manifest is the flat identity axis.
 *
 * Variant name convention: `${flavor}${BuildType}` (build-type first letter
 * upper-cased). Iteration is alphabetically sorted on both axes for
 * deterministic output ordering, which keeps the CI matrix reproducible and
 * `git diff`-friendly across successive runs.
 *
 * Run manually: `./gradlew :cmp-shared:exportKmpFlavorsManifest`
 */
@DisableCachingByDefault(because = "Reads live DSL state at execution time; regenerated every run so downstream CI is never stale")
abstract class ExportKmpFlavorsManifestTask : DefaultTask() {

    /**
     * flavor name → Android `applicationIdSuffix` (e.g. `"demo"` → `".demo"`,
     * `"prod"` → `""`). Empty string when the flavor declares no suffix.
     */
    @get:Input
    abstract val flavorAppIdSuffixes: MapProperty<String, String>

    /**
     * flavor name → iOS `bundleIdSuffix` (e.g. `"demo"` → `".demo"`,
     * `"prod"` → `""`). Empty string when the flavor declares no suffix.
     */
    @get:Input
    abstract val flavorBundleIdSuffixes: MapProperty<String, String>

    /**
     * build-type name → Android `applicationIdSuffix` (e.g. `"debug"` →
     * `".debug"`, `"staging"` → `".staging"`, `"release"` → `""`).
     */
    @get:Input
    abstract val buildTypeAppIdSuffixes: MapProperty<String, String>

    /**
     * Destination path for the emitted `variants.json` manifest.
     * Convention (see [KMPFlavorsConventionPlugin]):
     * `build/kmp-flavors/variants.json` under the registering module (`cmp-shared`).
     * Gitignored by convention — the manifest is a build output regenerated at
     * CI runtime by the `resolve-variants` job.
     */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun export() {
        val flavors    = flavorAppIdSuffixes.get()
        val bundleIds  = flavorBundleIdSuffixes.get()
        val buildTypes = buildTypeAppIdSuffixes.get()

        val out = outputFile.get().asFile
        out.parentFile?.mkdirs()

        val json = buildManifestJson(flavors, bundleIds, buildTypes)
        out.writeText(json)

        logger.lifecycle(
            "exportKmpFlavorsManifest: wrote ${out.absolutePath} " +
                "(${flavors.size} flavors × ${buildTypes.size} buildTypes = " +
                "${flavors.size * buildTypes.size} variants)"
        )
    }

    /**
     * Assembles the `variants.json` payload from the three lazily-resolved
     * suffix maps. Keys are sorted alphabetically on both axes so the output
     * is deterministic across runs — a re-run against an unchanged DSL is a
     * byte-identical file (helpful for CI cache hits + `git diff` review).
     */
    private fun buildManifestJson(
        flavors: Map<String, String>,
        bundleIds: Map<String, String>,
        buildTypes: Map<String, String>,
    ): String {
        val sortedFlavors    = flavors.entries.sortedBy { it.key }
        val sortedBuildTypes = buildTypes.entries.sortedBy { it.key }

        val flavorsJson = sortedFlavors.joinToString(
            separator = ",\n    ",
            prefix    = "[\n    ",
            postfix   = "\n  ]",
        ) { (name, appIdSuffix) ->
            val bundleSuffix = bundleIds[name] ?: ""
            "{ " +
                "\"name\": \"${jsonEscape(name)}\", " +
                "\"applicationIdSuffix\": \"${jsonEscape(appIdSuffix)}\", " +
                "\"bundleIdSuffix\": \"${jsonEscape(bundleSuffix)}\"" +
                " }"
        }

        val buildTypesJson = sortedBuildTypes.joinToString(
            separator = ",\n    ",
            prefix    = "[\n    ",
            postfix   = "\n  ]",
        ) { (name, appIdSuffix) ->
            "{ " +
                "\"name\": \"${jsonEscape(name)}\", " +
                "\"applicationIdSuffix\": \"${jsonEscape(appIdSuffix)}\"" +
                " }"
        }

        val variantRows = sortedFlavors.flatMap { (flavor, _) ->
            sortedBuildTypes.map { (buildType, _) ->
                val name = "$flavor${buildType.replaceFirstChar { it.uppercase() }}"
                "    { " +
                    "\"name\": \"${jsonEscape(name)}\", " +
                    "\"flavor\": \"${jsonEscape(flavor)}\", " +
                    "\"buildType\": \"${jsonEscape(buildType)}\"" +
                    " }"
            }
        }
        val variantsJson = variantRows.joinToString(
            separator = ",\n",
            prefix    = "[\n",
            postfix   = "\n  ]",
        )

        return """
            |{
            |  "schema_version": 1,
            |  "flavors": $flavorsJson,
            |  "buildTypes": $buildTypesJson,
            |  "variants": $variantsJson
            |}
            |
        """.trimMargin()
    }

    /**
     * Minimal JSON-string escape for the identifier-shaped values this task
     * writes (flavor / build-type names + suffix values). The DSL enforces
     * Gradle identifier rules on flavor / build-type names + convention-safe
     * dot-prefixed suffixes, so backslash / double-quote / control chars are
     * effectively impossible — but the escape is still applied defensively so
     * any future DSL widening does not silently emit malformed JSON.
     */
    private fun jsonEscape(value: String): String {
        val sb = StringBuilder(value.length)
        for (ch in value) {
            when (ch) {
                '\\'     -> sb.append("\\\\")
                '"'      -> sb.append("\\\"")
                '\n'     -> sb.append("\\n")
                '\r'     -> sb.append("\\r")
                '\t'     -> sb.append("\\t")
                '\b'     -> sb.append("\\b")
                else -> {
                    // Form-feed (U+000C) + any other C0 control char fall through here
                    // and emit as \uXXXX — still valid JSON per RFC 8259 §7.
                    if (ch.code < 0x20) {
                        sb.append("\\u").append("%04x".format(ch.code))
                    } else {
                        sb.append(ch)
                    }
                }
            }
        }
        return sb.toString()
    }
}
