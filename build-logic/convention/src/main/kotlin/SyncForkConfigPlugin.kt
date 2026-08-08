import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
import org.yaml.snakeyaml.Yaml
import java.io.File

/**
 * Registers the `syncForkConfig` task on whichever project applies this plugin.
 *
 * Single source of truth:
 *   gradle/fork.properties    — deployment identity + store metadata (gitignored)
 *   gradle/libs.versions.toml — 6 build-time Gradle values (appId, appDisplayName,
 *                               baseNamespace, desktopAppName, projectName, iosTeamId)
 *
 * Propagates ALL fork identity to:
 *   - cmp-ios/Configuration/Config.xcconfig
 *   - local.properties          (Fastlane bridge — gitignored)
 *   - gradle.properties
 *   - All store metadata .txt files (App Store iOS, App Store macOS, Play Store)
 *   - Platform app icons (from app-profile/icons/)
 *
 * Apply from root build.gradle.kts:
 *   plugins { id("org.convention.fork.sync-config") }
 *
 * Then run:
 *   ./gradlew syncForkConfig
 */
class SyncForkConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register<SyncForkConfigTask>("syncForkConfig") {
            group       = "fork"
            description = "Sync fork identity from fork.properties + libs.versions.toml to all platform config files."
            // Always re-run — reads gitignored fork.properties, not tracked by Gradle.
            outputs.upToDateWhen { false }
            projectRootDir.set(target.layout.projectDirectory)
            iconSourceDir.set(target.layout.projectDirectory.dir("app-profile/icons"))
            iosAppIconDir.set(target.layout.projectDirectory.dir("cmp-ios/iosApp/Assets.xcassets/AppIcon.appiconset"))
            jsResourcesDir.set(target.layout.projectDirectory.dir("cmp-web/src/jsMain/resources"))
            wasmJsResourcesDir.set(target.layout.projectDirectory.dir("cmp-web/src/wasmJsMain/resources"))
            desktopIconsDir.set(target.layout.projectDirectory.dir("cmp-desktop/icons"))
            androidResDir.set(target.layout.projectDirectory.dir("cmp-android/src/main/res"))
        }
    }
}

@DisableCachingByDefault(because = "Reads gitignored fork.properties; writes local.properties and metadata files")
abstract class SyncForkConfigTask : DefaultTask() {

    @get:Internal abstract val projectRootDir:     DirectoryProperty
    @get:Internal abstract val iconSourceDir:    DirectoryProperty
    @get:Internal abstract val iosAppIconDir:      DirectoryProperty
    @get:Internal abstract val jsResourcesDir:     DirectoryProperty
    @get:Internal abstract val wasmJsResourcesDir: DirectoryProperty
    @get:Internal abstract val desktopIconsDir:    DirectoryProperty
    @get:Internal abstract val androidResDir:      DirectoryProperty

    @TaskAction
    fun sync() {
        val root = projectRootDir.get().asFile

        // ── 1. Load gradle/fork.properties ──────────────────────────────────
        val fork = java.util.Properties()
        val forkFile = File(root, "gradle/fork.properties")
        if (forkFile.exists()) {
            forkFile.reader(Charsets.UTF_8).use(fork::load)
        } else {
            logger.warn(
                "syncForkConfig: gradle/fork.properties not found.\n" +
                "  Copy: cp gradle/fork.properties.template gradle/fork.properties\n" +
                "  Fill in your values, then re-run ./gradlew syncForkConfig"
            )
        }

        // ── 2. Load 6 build-time fields from gradle/libs.versions.toml ──────
        val toml = parseTomlVersions(File(root, "gradle/libs.versions.toml"))

        // ── 2a. Load app-profile/ — the fork-owned white-label SoT ──────────
        // Deep-merges app.yaml + platforms/**/*.yaml into one nested map and resolves the SAME
        // flat dotted fork.properties keys the Ruby side (deployment/_shared/config.rb AppProfile)
        // resolves — one contract, two consumers. Fail-soft: absent app-profile/ → empty map, so
        // the plugin behaves EXACTLY as before (off fork.properties + TOML).
        val appProfile = loadAppProfile(root)
        val hasAppProfile = appProfile.isNotEmpty()

        // Priority: ENV > app-profile > fork.properties > TOML > ""
        fun get(forkKey: String, envKey: String? = null, tomlKey: String? = null): String {
            envKey?.let { System.getenv(it)?.takeIf(String::isNotBlank) }?.let { return it }
            appProfileGet(appProfile, forkKey)?.takeIf(String::isNotBlank)?.let { return it }
            (fork.getProperty(forkKey))?.takeIf(String::isNotBlank)?.let { return it }
            tomlKey?.let { toml[it]?.takeIf(String::isNotBlank) }?.let { return it }
            return ""
        }

        // Build-time fields. app.id is AUTHORED in fork.properties (its single source of truth);
        // appDisplayName/projectName still read fork.properties-first then fall back to TOML.
        val appId          = get("app.id",           "APP_BUNDLE_ID",   "appId")
        val appDisplayName = get("app.display.name", "APP_DISPLAY_NAME","appDisplayName")
        val projectName    = get("project.name",     "PROJECT_NAME",    "projectName")

        // ── 2b. Write app.id BACK into gradle/libs.versions.toml#appId ─────────
        // The whole build reads the bundle id via libs.versions.appId; fork.properties#app.id is the
        // authored SoT. Sync the catalog from it so a fork edits app.id in ONE place. No-op when they
        // already agree (e.g. the upstream template). product-health's appid-consistency check guards drift.
        if (appId.isNotBlank()) {
            patchTomlVersion(File(root, "gradle/libs.versions.toml"), "appId", appId)
        }

        // Apple
        val appleTeamId   = get("apple.team.id",     "APPLE_TEAM_ID",   "iosTeamId")
        val matchGitUrl   = get("apple.match.git.url","MATCH_GIT_URL")
        val tfGroups      = get("apple.tf.groups",    "TESTFLIGHT_GROUPS")

        // Firebase
        val fbAndroidProd = get("firebase.android.prod.app.id","FIREBASE_ANDROID_PROD_APP_ID")
        val fbAndroidDemo = get("firebase.android.demo.app.id","FIREBASE_ANDROID_DEMO_APP_ID")
        val fbIos         = get("firebase.ios.app.id",         "FIREBASE_IOS_APP_ID")
        val fbGroups      = get("firebase.groups",             "FIREBASE_GROUPS")

        // Org
        val orgName      = get("org.name",         "ORGANIZATION_NAME")
        val orgEmail     = get("org.email",        "APPSTORE_REVIEW_EMAIL")
        val orgFirstName = get("org.first.name",   "APPSTORE_REVIEW_FIRST_NAME")
        val orgLastName  = get("org.last.name",    "APPSTORE_REVIEW_LAST_NAME")
        val orgPhone     = get("org.phone",        "APPSTORE_REVIEW_PHONE")
        val marketingUrl = get("org.marketing.url","APP_MARKETING_URL")
        val privacyUrl   = get("org.privacy.url",  "APP_PRIVACY_URL")
        val supportUrl   = get("org.support.url")

        // Web
        val cloudflareProject = get("web.cloudflare.project","CLOUDFLARE_PAGES_PROJECT_NAME")

        // Store — shared
        // Primary listing locale (metadata subdir). app-profile store.primary_locale is the SoT;
        // default en-US. Replaces the hardcoded en-GB (iOS/mac) / en-US (android) subdirs.
        val primaryLocale      = get("store.primary.locale").ifBlank { "en-US" }
        val storeTitle         = get("store.title")
        val storeSubtitle      = get("store.subtitle")
        val storeDescription   = get("store.description")
        val storePromoText     = get("store.promotional.text")
        val storeReleaseNotes  = get("store.release.notes")
        val storeCopyright     = get("store.copyright")
        val storeReviewNotes   = get("store.review.notes")
        val reviewDemoUser     = get("store.review.demo.user")
        val reviewDemoPassword = get("store.review.demo.password")
        val iosAgeRating       = get("store.ios.age.rating").ifBlank { "4+" }

        // Store — Apple trade representative contact information (App Store Connect requirement)
        val tradeFirstName  = get("store.apple.trade.first.name")
        val tradeLastName   = get("store.apple.trade.last.name")
        val tradeAddr1      = get("store.apple.trade.address.line1")
        val tradeAddr2      = get("store.apple.trade.address.line2")
        val tradeAddr3      = get("store.apple.trade.address.line3")
        val tradeCity       = get("store.apple.trade.city")
        val tradeState      = get("store.apple.trade.state")
        val tradeCountry    = get("store.apple.trade.country")
        val tradePostal     = get("store.apple.trade.postal.code")
        val tradePhone      = get("store.apple.trade.phone")
        val tradeEmail      = get("store.apple.trade.email")
        val tradeDisplayed  = get("store.apple.trade.displayed")

        // Store — Android
        val androidChangelog  = get("store.android.changelog")
        val androidShortDesc  = get("store.android.short.description")
        val androidCategory   = get("store.android.category")
        val androidVideoUrl   = get("store.android.video.url")

        // Store — iOS
        val iosKeywords           = get("store.ios.keywords")
        val iosCategory           = get("store.ios.category")
        val iosSecondaryCategory  = get("store.ios.secondary.category")
        val iosAppleTvPrivacyUrl  = get("store.ios.apple.tv.privacy.url")

        // Store — macOS
        val macosKeywords          = get("store.macos.keywords")
        val macosCategory          = get("store.macos.category")
        val macosSecondaryCategory = get("store.macos.secondary.category")

        // Store — Windows / Microsoft Store
        val msAppId      = get("store.windows.ms.app.id", "MS_APP_ID")
        val msPublishMode = get("store.windows.ms.publish.mode").ifBlank { "Manual" }
        val msVisibility  = get("store.windows.ms.visibility").ifBlank { "Public" }
        // MSIX packaging identity (Package.appxmanifest) — app-profile windows.msix_*.
        val msixIdentityName    = get("windows.msix.identity.name")
        val msixIdentityPub     = get("windows.msix.identity.publisher")
        val msixPublisherDisplay = get("windows.msix.publisher.display.name")

        // ── 3. iOS xcconfig ───────────────────────────────────────────────────
        val xcconfigFile = File(root, "cmp-ios/Configuration/Config.xcconfig")
        if (xcconfigFile.parentFile.exists()) {
            xcconfigFile.writeText(
                "// Fork identity — generated by ./gradlew syncForkConfig\n" +
                "// To change: edit gradle/fork.properties (or libs.versions.toml for app.id),\n" +
                "// then re-run ./gradlew syncForkConfig\n" +
                "APP_BUNDLE_ID = $appId\n" +
                "APP_NAME = $appDisplayName\n" +
                "TEAM_ID = $appleTeamId\n"
            )
            logger.lifecycle("syncForkConfig: wrote cmp-ios/Configuration/Config.xcconfig")
        }
        // NOTE: cmp-ios/Configs/{variant}.xcconfig files are generated by the
        // :cmp-shared:generateIosFlavorXcconfigs task (KMPFlavorsConventionPlugin),
        // which reads the live kmpFlavors DSL. They auto-regenerate on every iOS build.
        // syncForkConfig only owns Config.xcconfig (APP_BUNDLE_ID / APP_NAME / TEAM_ID).

        // ── 4. local.properties (Fastlane bridge — gitignored) ───────────────
        // project_config.rb reads these as a fallback when fork.properties is absent.
        val lp    = File(root, "local.properties")
        val props = java.util.Properties()
        if (lp.exists()) lp.inputStream().use(props::load)
        // Write every fork.properties key with fork.* prefix
        fork.forEach { k, v -> props["fork.$k"] = v }
        // Ensure the 4 TOML-sourced keys are always present
        if (appId.isNotBlank())          props["fork.app.id"]           = appId
        if (appDisplayName.isNotBlank()) props["fork.app.display.name"] = appDisplayName
        if (projectName.isNotBlank())    props["fork.project.name"]     = projectName
        if (appleTeamId.isNotBlank())    props["fork.apple.team.id"]    = appleTeamId
        lp.outputStream().use { props.store(it, "Generated by syncForkConfig — do not edit manually") }
        logger.lifecycle("syncForkConfig: updated local.properties (${props.size} entries)")

        // ── 5. gradle.properties (settings.gradle.kts bridge) ────────────────
        val gp     = File(root, "gradle.properties")
        val gpText = if (gp.exists()) gp.readText() else ""
        gp.writeText(
            if (gpText.contains("fork.project.name="))
                gpText.replace(Regex("fork\\.project\\.name=.*"), "fork.project.name=$projectName")
            else
                gpText.trimEnd() + "\nfork.project.name=$projectName\n"
        )
        logger.lifecycle("syncForkConfig: updated gradle.properties fork.project.name=$projectName")

        // ── 5b. Regenerate gradle/fork.properties FROM resolved values ────────
        // When app-profile/ is the SoT, fork.properties becomes a DERIVED build-bridge so anything
        // that reads it directly (e.g. AndroidConfig::PRIMARY_LOCALE in config.rb, which parses
        // store.primary.locale inline) stays fed from app-profile. Every key the plugin resolves is
        // written; unknown/legacy fork.properties keys (apple.tf.groups, apple.testers.*, …) are
        // PRESERVED so config.rb's fallback still resolves them. Values are written RAW (not via
        // Properties.store) so config.rb's raw `split("=")` reader sees the exact string — app.yaml
        // uses single-line `\n`-literal scalars, so no value spans multiple lines. Only runs when
        // app-profile/ is present; a fork WITHOUT app-profile keeps its hand-authored file untouched.
        if (hasAppProfile) {
            val resolved = linkedMapOf(
                "app.id" to appId, "app.display.name" to appDisplayName, "project.name" to projectName,
                "apple.team.id" to appleTeamId, "apple.match.git.url" to matchGitUrl,
                "apple.tf.groups" to tfGroups,
                "firebase.android.prod.app.id" to fbAndroidProd,
                "firebase.android.demo.app.id" to fbAndroidDemo,
                "firebase.ios.app.id" to fbIos, "firebase.groups" to fbGroups,
                "org.name" to orgName, "org.email" to orgEmail, "org.first.name" to orgFirstName,
                "org.last.name" to orgLastName, "org.phone" to orgPhone,
                "org.marketing.url" to marketingUrl, "org.privacy.url" to privacyUrl,
                "org.support.url" to supportUrl, "web.cloudflare.project" to cloudflareProject,
                "store.primary.locale" to primaryLocale, "store.title" to storeTitle,
                "store.subtitle" to storeSubtitle, "store.description" to storeDescription,
                "store.promotional.text" to storePromoText, "store.release.notes" to storeReleaseNotes,
                "store.copyright" to storeCopyright, "store.review.notes" to storeReviewNotes,
                "store.review.demo.user" to reviewDemoUser,
                "store.review.demo.password" to reviewDemoPassword,
                "store.ios.age.rating" to iosAgeRating, "store.android.changelog" to androidChangelog,
                "store.android.short.description" to androidShortDesc,
                "store.android.category" to androidCategory, "store.android.video.url" to androidVideoUrl,
                "store.ios.keywords" to iosKeywords, "store.ios.category" to iosCategory,
                "store.ios.secondary.category" to iosSecondaryCategory,
                "store.ios.apple.tv.privacy.url" to iosAppleTvPrivacyUrl,
                "store.macos.keywords" to macosKeywords, "store.macos.category" to macosCategory,
                "store.macos.secondary.category" to macosSecondaryCategory,
                "store.windows.ms.app.id" to msAppId, "store.windows.ms.publish.mode" to msPublishMode,
                "store.windows.ms.visibility" to msVisibility,
                "windows.msix.identity.name" to msixIdentityName,
                "windows.msix.identity.publisher" to msixIdentityPub,
                "windows.msix.publisher.display.name" to msixPublisherDisplay,
            )
            val forkOut = LinkedHashMap<String, String>()
            resolved.forEach { (k, v) -> if (v.isNotBlank()) forkOut[k] = v }
            // Preserve legacy/unknown keys already in fork.properties (single-line values only).
            fork.forEach { k, v ->
                val ks = k.toString()
                val vs = v.toString()
                if (!forkOut.containsKey(ks) && vs.isNotBlank() && !vs.contains('\n')) forkOut[ks] = vs
            }
            val sb = StringBuilder()
                .append("# GENERATED from app-profile/app.yaml by syncForkConfig — do not hand-edit\n")
                .append("# Edit app-profile/app.yaml or app-profile/platforms/**/*.yaml instead;\n")
                .append("# fork.properties is the derived build-bridge (config.rb fallback + inline reads).\n")
            forkOut.forEach { (k, v) -> sb.append(k).append('=').append(v).append('\n') }
            File(root, "gradle/fork.properties").writeText(sb.toString())
            logger.lifecycle("syncForkConfig: regenerated fork.properties from app-profile (${forkOut.size} keys)")
        }

        // ── 6. Store metadata files ───────────────────────────────────────────
        var written = 0

        // app.yaml stores long copy (store.description / release_notes / review notes / changelog)
        // as single-quoted scalars with LITERAL `\n` sequences (kept single-line so config.rb's raw
        // reader + fork.properties round-trip). Metadata .txt files want REAL newlines, so unescape
        // here — the point at which the plugin owns the .txt output (G4). No-op for single-line values.
        fun nl(value: String): String = value.replace("\\r\\n", "\n").replace("\\n", "\n")

        // Write only when value is non-blank (skip optional fields that aren't set)
        fun writeIfPresent(rel: String, value: String) {
            if (value.isBlank()) return
            val f = File(root, rel)
            if (!f.parentFile.exists()) return
            f.writeText(nl(value))
            logger.lifecycle("syncForkConfig: wrote $rel")
            written++
        }

        // Always write — clears file when value is blank (used for optional/empty fields
        // that must exist so Fastlane / Deliver don't fall back to stale content)
        fun writeAlways(rel: String, value: String) {
            val f = File(root, rel)
            if (!f.parentFile.exists()) return
            f.writeText(nl(value))
            logger.lifecycle("syncForkConfig: wrote $rel")
            written++
        }

        // Locale-scoped metadata writer (G5). Writes `<metaRoot>/<primaryLocale>/<name>` (the fresh,
        // authoritative copy) and ALSO refreshes a pre-existing legacy `<metaRoot>/en-GB/<name>` when
        // it is on disk and differs from the primary locale — back-compat for forks whose Deliver
        // config still points at en-GB. writeIfPresent already skips a missing parent dir.
        fun writeLocalized(metaRoot: String, name: String, value: String) {
            // Ensure the primary-locale dir exists so the listing derives even when the template ships
            // only a legacy locale (e.g. mac-app-store/metadata has en-GB but no en-US) — otherwise
            // writeIfPresent skips (parent-dir-missing) and the fork's macOS store name/desc stays blank.
            if (value.isNotBlank()) File(root, "$metaRoot/$primaryLocale").mkdirs()
            writeIfPresent("$metaRoot/$primaryLocale/$name", value)
            if (primaryLocale != "en-GB" && File(root, "$metaRoot/en-GB").isDirectory) {
                writeIfPresent("$metaRoot/en-GB/$name", value)
            }
        }

        // Generate App Store / Mac App Store age-rating JSON from store.ios.age.rating
        fun writeRatingConfig(rel: String) {
            val f = File(root, rel)
            if (!f.parentFile.exists()) return
            f.writeText("""{
  "v1_0": {
    "ratings": {
      "violenceCartoonOrFantasy": "NONE",
      "violenceRealistic": "NONE",
      "violenceRealisticProlongedGraphicOrSadistic": "NONE",
      "profanityOrCrudeHumor": "NONE",
      "matureOrSuggestiveThemes": "NONE",
      "horrorOrFearThemesForChildren": "NONE",
      "medicalOrTreatmentInformation": "NONE",
      "alcoholTobaccoOrDrugUseOrReferences": "NONE",
      "gamblingAndContests": "NONE",
      "sexualContentOrNudity": "NONE",
      "sexualContentGraphicAndNudity": "NONE"
    },
    "booleans": {
      "ageRatingProcess": false,
      "gamblingAllowed": false,
      "unrestrictedWebAccess": false,
      "kidsAgeBand": false
    }
  }
}""")
            logger.lifecycle("syncForkConfig: wrote $rel (age rating: $iosAgeRating)")
            written++
        }

        // ── iOS App Store ─────────────────────────────────────────────────────
        // Locale-scoped files → primaryLocale dir (was hardcoded en-GB) via writeLocalized (G5).
        writeLocalized("deployment/ios/appstore/metadata", "name.txt",             storeTitle)
        writeLocalized("deployment/ios/appstore/metadata", "subtitle.txt",         storeSubtitle)
        writeLocalized("deployment/ios/appstore/metadata", "description.txt",      storeDescription)
        writeLocalized("deployment/ios/appstore/metadata", "keywords.txt",         iosKeywords)
        writeLocalized("deployment/ios/appstore/metadata", "promotional_text.txt", storePromoText)
        writeLocalized("deployment/ios/appstore/metadata", "release_notes.txt",    storeReleaseNotes)
        writeLocalized("deployment/ios/appstore/metadata", "marketing_url.txt",    marketingUrl)
        writeLocalized("deployment/ios/appstore/metadata", "privacy_url.txt",      privacyUrl)
        writeLocalized("deployment/ios/appstore/metadata", "support_url.txt",      supportUrl)
        writeIfPresent("deployment/ios/appstore/metadata/copyright.txt",              storeCopyright)
        writeIfPresent("deployment/ios/appstore/metadata/primary_category.txt",       iosCategory)
        writeAlways("deployment/ios/appstore/metadata/primary_first_sub_category.txt",  "")
        writeAlways("deployment/ios/appstore/metadata/primary_second_sub_category.txt", "")
        writeAlways("deployment/ios/appstore/metadata/secondary_category.txt",          iosSecondaryCategory)
        writeAlways("deployment/ios/appstore/metadata/secondary_first_sub_category.txt",  "")
        writeAlways("deployment/ios/appstore/metadata/secondary_second_sub_category.txt", "")
        writeIfPresent("deployment/ios/appstore/metadata/review_information/email_address.txt", orgEmail)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/first_name.txt",    orgFirstName)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/last_name.txt",     orgLastName)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/phone_number.txt",  orgPhone)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/notes.txt",         storeReviewNotes)
        writeAlways("deployment/ios/appstore/metadata/review_information/demo_user.txt",     reviewDemoUser)
        writeAlways("deployment/ios/appstore/metadata/review_information/demo_password.txt", reviewDemoPassword)
        writeAlways("deployment/ios/appstore/metadata/$primaryLocale/apple_tv_privacy_policy.txt", iosAppleTvPrivacyUrl)
        writeRatingConfig("deployment/ios/appstore/metadata/app_store_rating_config.json")

        // ── iOS trade representative contact information (App Store Connect) ──
        val tradeIos = "deployment/ios/appstore/metadata/trade_representative_contact_information"
        File(root, tradeIos).mkdirs()
        writeIfPresent("$tradeIos/first_name.txt",   tradeFirstName)
        writeIfPresent("$tradeIos/last_name.txt",    tradeLastName)
        writeIfPresent("$tradeIos/address_line1.txt", tradeAddr1)
        writeAlways("$tradeIos/address_line2.txt",    tradeAddr2)
        writeAlways("$tradeIos/address_line3.txt",    tradeAddr3)
        writeIfPresent("$tradeIos/city_name.txt",    tradeCity)
        writeIfPresent("$tradeIos/state.txt",        tradeState)
        writeIfPresent("$tradeIos/country.txt",      tradeCountry)
        writeIfPresent("$tradeIos/postal_code.txt",  tradePostal)
        writeIfPresent("$tradeIos/phone_number.txt", tradePhone)
        writeIfPresent("$tradeIos/email_address.txt", tradeEmail)
        writeAlways("$tradeIos/is_displayed_on_app_store.txt", tradeDisplayed)

        // ── macOS App Store ───────────────────────────────────────────────────
        // Locale-scoped files → primaryLocale dir (was hardcoded en-GB) via writeLocalized (G5).
        writeLocalized("deployment/desktop/mac-app-store/metadata", "name.txt",             storeTitle)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "subtitle.txt",         storeSubtitle)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "description.txt",      storeDescription)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "keywords.txt",         macosKeywords)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "promotional_text.txt", storePromoText)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "release_notes.txt",    storeReleaseNotes)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "marketing_url.txt",    marketingUrl)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "privacy_url.txt",      privacyUrl)
        writeLocalized("deployment/desktop/mac-app-store/metadata", "support_url.txt",      supportUrl)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/copyright.txt",              storeCopyright)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/primary_category.txt",       macosCategory)
        writeAlways("deployment/desktop/mac-app-store/metadata/primary_first_sub_category.txt",  "")
        writeAlways("deployment/desktop/mac-app-store/metadata/primary_second_sub_category.txt", "")
        writeAlways("deployment/desktop/mac-app-store/metadata/secondary_category.txt",          macosSecondaryCategory)
        writeAlways("deployment/desktop/mac-app-store/metadata/secondary_first_sub_category.txt",  "")
        writeAlways("deployment/desktop/mac-app-store/metadata/secondary_second_sub_category.txt", "")
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/email_address.txt", orgEmail)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/first_name.txt",    orgFirstName)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/last_name.txt",     orgLastName)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/phone_number.txt",  orgPhone)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/notes.txt",         storeReviewNotes)
        writeAlways("deployment/desktop/mac-app-store/metadata/review_information/demo_user.txt",     reviewDemoUser)
        writeAlways("deployment/desktop/mac-app-store/metadata/review_information/demo_password.txt", reviewDemoPassword)
        writeRatingConfig("deployment/desktop/mac-app-store/metadata/app_store_rating_config.json")

        // ── macOS trade representative contact information (App Store Connect) ─
        val tradeMac = "deployment/desktop/mac-app-store/metadata/trade_representative_contact_information"
        File(root, tradeMac).mkdirs()
        writeIfPresent("$tradeMac/first_name.txt",   tradeFirstName)
        writeIfPresent("$tradeMac/last_name.txt",    tradeLastName)
        writeIfPresent("$tradeMac/address_line1.txt", tradeAddr1)
        writeAlways("$tradeMac/address_line2.txt",    tradeAddr2)
        writeAlways("$tradeMac/address_line3.txt",    tradeAddr3)
        writeIfPresent("$tradeMac/city_name.txt",    tradeCity)
        writeIfPresent("$tradeMac/state.txt",        tradeState)
        writeIfPresent("$tradeMac/country.txt",      tradeCountry)
        writeIfPresent("$tradeMac/postal_code.txt",  tradePostal)
        writeIfPresent("$tradeMac/phone_number.txt", tradePhone)
        writeIfPresent("$tradeMac/email_address.txt", tradeEmail)
        writeAlways("$tradeMac/is_displayed_on_app_store.txt", tradeDisplayed)

        // ── Android Play Store ────────────────────────────────────────────────
        // Locale-scoped files → primaryLocale dir (default en-US) — G5. Android uses en-US today,
        // so the default is a no-op; a fork that sets store.primary_locale moves the listing subdir.
        writeIfPresent("deployment/android/metadata/$primaryLocale/title.txt",              storeTitle)
        writeIfPresent("deployment/android/metadata/$primaryLocale/short_description.txt",  androidShortDesc)
        writeIfPresent("deployment/android/metadata/$primaryLocale/full_description.txt",   storeDescription)
        writeIfPresent("deployment/android/metadata/$primaryLocale/changelogs/default.txt", androidChangelog)
        writeAlways("deployment/android/metadata/$primaryLocale/video.txt", androidVideoUrl)

        // ── Windows / Microsoft Store ─────────────────────────────────────────
        val storeBrokerFile = File(root, "deployment/desktop/microsoft-store/StoreBroker.config.json")
        if (storeBrokerFile.parentFile.exists()) {
            val resolvedAppId = msAppId.ifBlank { "\${ENV:MS_APP_ID}" }
            val msixName = if (projectName.isNotBlank()) "$projectName.msix" else "app.msix"
            storeBrokerFile.writeText("""{
  "_comment": "StoreBroker submission config — generated by ./gradlew syncForkConfig. Edit gradle/fork.properties to change.",
  "appId": "$resolvedAppId",
  "flightId": null,
  "packagePath": "cmp-desktop/build/compose/binaries/main-release/msix/$msixName",
  "targetPublishMode": "$msPublishMode",
  "visibility": "$msVisibility"
}""")
            logger.lifecycle("syncForkConfig: wrote deployment/desktop/microsoft-store/StoreBroker.config.json")
            written++
        }

        // ── 6c. Tokenize template-owned deployment files (B2 / G6) ────────────
        // These files carry identity LITERALS (mifos-x-web / MifosInitiative.MoneyToolkit / the
        // kmp-project-template keystore-alias prefix / …) in the committed template. Derive them from
        // app-profile (+ projectName) via targeted substitution — NOT full-file rewrite — so the file
        // structure is preserved and no store-bound literal survives for a fork.
        written += tokenizeDeploymentFiles(
            root, cloudflareProject, msixIdentityName, msixIdentityPub, msixPublisherDisplay, appDisplayName,
            projectName,
        )

        logger.lifecycle("syncForkConfig: wrote $written store metadata files")

        // ── 6d. Fork media fan-out (platforms-first, extends the icon pattern) ─
        fun deriveForkMedia() {
            // platforms/<p>/media/ is the fork-owned media SoT → deployment/<p>/metadata images (extends the icon pattern).
            val map = mapOf(
                "android" to "deployment/android/metadata",
                "apple/ios" to "deployment/ios/appstore/metadata",
                "apple/macos" to "deployment/desktop/mac-app-store/metadata",
                "windows" to "deployment/windows/metadata",
                "ubuntu" to "deployment/linux/metadata",
            )
            for ((platRel, metaRel) in map) {
                val src = File(root, "app-profile/platforms/$platRel/media")
                if (!src.isDirectory) continue
                // Skip a media dir with no real assets (only .gitkeep) — don't seed empty deployment metadata dirs.
                if (src.walkTopDown().none { it.isFile && it.name != ".gitkeep" }) continue
                val dst = File(root, metaRel); dst.mkdirs()
                src.copyRecursively(dst, overwrite = true)
            }
            // Play caps screenshots at 8 per form-factor per locale — trim deployment/android/metadata; extras → _over8/.
            val androidMeta = File(root, "deployment/android/metadata")
            androidMeta.listFiles()?.filter { it.isDirectory }?.forEach { locale ->
                listOf("phoneScreenshots", "sevenInchScreenshots", "tenInchScreenshots").forEach { form ->
                    val dir = File(locale, "images/$form")
                    if (dir.isDirectory) {
                        val imgs = dir.listFiles { f -> f.isFile && f.name.matches(Regex(".*\\.(png|jpe?g)$", RegexOption.IGNORE_CASE)) }?.sortedBy { it.name } ?: emptyList()
                        if (imgs.size > 8) { val over = File(locale, "images/_over8/$form"); over.mkdirs(); imgs.drop(8).forEach { it.renameTo(File(over, it.name)) } }
                    }
                }
            }
        }
        deriveForkMedia()

        // ── 6e. Copy managed structured docs to deployment ─────────────────────
        fun copyIfExists(srcRel: String, dstRel: String) {
            val s = File(root, srcRel); if (!s.isFile) return
            val d = File(root, dstRel); d.parentFile?.mkdirs(); s.copyTo(d, overwrite = true)
        }
        copyIfExists("app-profile/platforms/apple/ios/app-content/app_privacy_details.json", "deployment/ios/appstore/metadata/app_privacy_details.json")
        copyIfExists("app-profile/platforms/apple/macos/app-content/app_privacy_details.json", "deployment/desktop/mac-app-store/metadata/app_privacy_details.json")
        copyIfExists("app-profile/platforms/android/app-content/data-safety.csv", "deployment/android/app-content/data-safety.csv")

        // ── 6b. Fork-unique database filename (core/database/DatabaseConfig.kt) ─
        // Derive a per-fork on-disk DB name from appId (dots → underscores) so two
        // forks never collide on desktop/web storage. Uses appId (NOT baseNamespace).
        if (appId.isNotBlank()) {
            val dbName = appId.lowercase().replace(Regex("[^a-z0-9]"), "_") + ".db"
            val dbConfig = File(root, "core/database/src/commonMain/kotlin/kpt/core/database/DatabaseConfig.kt")
            if (dbConfig.parentFile.exists()) {
                dbConfig.writeText(
                    "/*\n" +
                        " * Copyright 2026 Mifos Initiative\n" +
                        " *\n" +
                        " * This Source Code Form is subject to the terms of the Mozilla Public\n" +
                        " * License, v. 2.0. If a copy of the MPL was not distributed with this\n" +
                        " * file, You can obtain one at https://mozilla.org/MPL/2.0/.\n" +
                        " *\n" +
                        " * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE\n" +
                        " */\n" +
                        "package kpt.core.database\n\n" +
                        "/** Fork-unique on-disk DB filename (appId-derived) — generated by syncForkConfig. */\n" +
                        "object DatabaseConfig {\n" +
                        "    const val NAME = \"$dbName\"\n" +
                        "}\n",
                )
                logger.lifecycle("syncForkConfig: wrote core/database/.../DatabaseConfig.kt (NAME=$dbName)")
            }
        }

        // ── 7. Fork icons ─────────────────────────────────────────────────────
        copyForkIcons(root)
    }

    /**
     * Replace `key = "..."` in libs.versions.toml with [value], preserving the leading alignment
     * and any trailing inline `# comment`. No-op when the file is missing, the key isn't found, or
     * the value already matches (so the committed catalog only churns on a real fork identity change).
     */
    private fun patchTomlVersion(toml: File, key: String, value: String) {
        if (!toml.exists()) return
        val lines = toml.readLines()
        // ^(indent + key + spaces + = + spaces)("old")(rest incl. inline comment)$  — key match is exact
        // (\b guards against appId matching a longer key), so appDisplayName is never touched.
        val re = Regex("^(\\s*" + Regex.escape(key) + "\\s*=\\s*)\"[^\"]*\"(.*)$")
        var hit = false
        val out = lines.map { line ->
            val m = re.find(line) ?: return@map line
            hit = true
            "${m.groupValues[1]}\"$value\"${m.groupValues[2]}"
        }
        if (hit && out != lines) {
            toml.writeText(out.joinToString("\n") + "\n")
            logger.lifecycle("syncForkConfig: patched gradle/libs.versions.toml $key=\"$value\" (from fork.properties)")
        }
    }

    // ── app-profile/ loader (fork-owned white-label SoT) ──────────────────────
    // Reads app-profile/app.yaml + every platforms/**/*.yaml, deep-merged (app.yaml first, then
    // platform files sorted by path so later overrides earlier) into ONE nested String-keyed map.
    // Mirrors the Ruby AppProfile._data merge order in deployment/_shared/config.rb exactly.
    // Fail-soft: absent dir → empty map; a malformed file is skipped with a warning, never fatal.
    private fun loadAppProfile(root: File): Map<String, Any?> {
        val dir = File(root, "app-profile")
        if (!dir.isDirectory) return emptyMap()
        val files = mutableListOf<File>()
        File(dir, "app.yaml").takeIf { it.isFile }?.let { files += it }
        File(dir, "platforms").takeIf { it.isDirectory }
            ?.walkTopDown()
            // Merge ONLY the <platform>.yaml CONFIG files — never the app-content/ store DECLARATIONS
            // (content-rating/age-rating/privacy/export-compliance are questionnaire answers read by
            //  their own consumers, not identity/store config keys — they must not pollute the map).
            ?.filter { it.isFile && it.extension == "yaml" && !it.path.contains("/app-content/") }
            ?.sortedBy { it.path }
            ?.forEach { files += it }
        var merged = emptyMap<String, Any?>()
        val yaml = Yaml()
        for (f in files) {
            try {
                val loaded = f.inputStream().use { yaml.load<Any?>(it) }
                if (loaded is Map<*, *>) merged = deepMerge(merged, loaded.toStringKeyedMap())
            } catch (e: Exception) {
                logger.warn("syncForkConfig: skipping malformed app-profile file ${f.name}: ${e.message}")
            }
        }
        return merged
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<*, *>.toStringKeyedMap(): Map<String, Any?> =
        entries.associate { (k, v) -> k.toString() to v }

    private fun deepMerge(a: Map<String, Any?>, b: Map<String, Any?>): Map<String, Any?> {
        val out = LinkedHashMap<String, Any?>(a)
        for ((k, v) in b) {
            val existing = out[k]
            out[k] = if (existing is Map<*, *> && v is Map<*, *>) {
                deepMerge(existing.toStringKeyedMap(), v.toStringKeyedMap())
            } else {
                v
            }
        }
        return out
    }

    // Resolve a flat fork.properties key against the merged app-profile map, via APP_PROFILE_MAP
    // (mirrors the Ruby AppProfile::MAP key-for-key). Returns null for an unmapped key or a missing
    // path; a container (map/list) leaf → null; a scalar leaf → its String form (mirrors Ruby to_s).
    private fun appProfileGet(data: Map<String, Any?>, key: String): String? {
        val path = APP_PROFILE_MAP[key] ?: return null
        var node: Any? = data
        for (seg in path.split(".")) {
            val m = node as? Map<*, *> ?: return null
            if (!m.containsKey(seg)) return null
            node = m[seg]
        }
        if (node is Map<*, *> || node is List<*>) return null
        return node?.toString()
    }

    // Targeted substitution of identity literals in template-owned deployment files (B2 / G6).
    // Regex-replaces only the specific attribute/line values, preserving each file's structure.
    // Returns the count of files rewritten. Skips a file when it is absent or its source value blank.
    private fun tokenizeDeploymentFiles(
        root: File,
        cloudflareProject: String,
        msixIdentityName: String,
        msixIdentityPub: String,
        msixPublisherDisplay: String,
        appName: String,
        aliasNamespace: String,
    ): Int {
        var count = 0

        // wrangler.toml — `name = "…"` (the Cloudflare Pages project name).
        if (cloudflareProject.isNotBlank()) {
            val wrangler = File(root, "deployment/web/cloudflare-pages/wrangler.toml")
            if (wrangler.isFile) {
                val re = Regex("(?m)^(\\s*name\\s*=\\s*)\"[^\"]*\"")
                val orig = wrangler.readText()
                val next = orig.replace(re) { "${it.groupValues[1]}\"$cloudflareProject\"" }
                if (next != orig) { wrangler.writeText(next); count++ }
                logger.lifecycle("syncForkConfig: tokenized wrangler.toml name=$cloudflareProject")
            }
            // config.yaml + workflow-snippet.yml — `--project-name=…` in the runner/CI command string.
            // (workflow-snippet.yml was missed initially — surfaced by the awaazly fork proof 2026-08-07.)
            for (rel in listOf(
                "deployment/web/cloudflare-pages/config.yaml",
                "deployment/web/cloudflare-pages/workflow-snippet.yml",
            )) {
                val f = File(root, rel)
                if (f.isFile) {
                    val re = Regex("(--project-name=)[^\"\\s]+")
                    val orig = f.readText()
                    val next = orig.replace(re) { "${it.groupValues[1]}$cloudflareProject" }
                    if (next != orig) { f.writeText(next); count++ }
                }
            }
        }

        // Package.appxmanifest — Identity Name/Publisher + PublisherDisplayName + Description.
        val appx = File(root, "deployment/desktop/microsoft-store/Package.appxmanifest")
        if (appx.isFile) {
            var text = appx.readText()
            val before = text
            if (msixIdentityName.isNotBlank()) {
                text = text.replace(Regex("(<Identity\\b[\\s\\S]*?\\bName=\")[^\"]*(\")")) {
                    "${it.groupValues[1]}$msixIdentityName${it.groupValues[2]}"
                }
            }
            if (msixIdentityPub.isNotBlank()) {
                text = text.replace(Regex("(<Identity\\b[\\s\\S]*?\\bPublisher=\")[^\"]*(\")")) {
                    "${it.groupValues[1]}$msixIdentityPub${it.groupValues[2]}"
                }
            }
            if (msixPublisherDisplay.isNotBlank()) {
                text = text.replace(Regex("(<PublisherDisplayName>)[^<]*(</PublisherDisplayName>)")) {
                    "${it.groupValues[1]}$msixPublisherDisplay${it.groupValues[2]}"
                }
            }
            if (appName.isNotBlank()) {
                text = text.replace(Regex("(\\bDescription=\")[^\"]*(\")")) {
                    "${it.groupValues[1]}$appName${it.groupValues[2]}"
                }
            }
            if (text != before) { appx.writeText(text); count++ }
            logger.lifecycle("syncForkConfig: tokenized Package.appxmanifest")
        }

        // deployment/android/*/secrets-needs.yaml — vault-alias prefix (G2). The committed template
        // hardcodes `kmp-project-template-upload-keystore[-*]`; derive the prefix from the fork's
        // project slug (projectName) so each fork's keystore aliases are unique + rename cleanly on
        // sync. No-op on the upstream template (projectName == "kmp-project-template"). The 4 suffixed
        // aliases (-storepass/-keyalias/-keypass) share the base token, so replacing the whole
        // `<slug>-upload-keystore` token rewrites every alias line in one pass. Fail-soft when absent.
        if (aliasNamespace.isNotBlank()) {
            File(root, "deployment/android").listFiles()
                ?.filter { it.isDirectory }
                ?.sortedBy { it.name }
                ?.forEach { d ->
                    val f = File(d, "secrets-needs.yaml")
                    if (!f.isFile) return@forEach
                    val orig = f.readText()
                    val next = orig.replace("kmp-project-template-upload-keystore", "$aliasNamespace-upload-keystore")
                    if (next != orig) {
                        f.writeText(next); count++
                        logger.lifecycle("syncForkConfig: tokenized ${f.relativeTo(root)} (alias prefix=$aliasNamespace)")
                    }
                }
        }

        return count
    }

    private fun parseTomlVersions(toml: File): Map<String, String> {
        if (!toml.exists()) return emptyMap()
        val result = mutableMapOf<String, String>()
        toml.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || !trimmed.contains("=")) return@forEach
            val eq = trimmed.indexOf('=')
            val key = trimmed.substring(0, eq).trim()
            // Drop inline TOML comment first, then strip quotes
            val raw = trimmed.substring(eq + 1).trim().substringBefore(" #").trim()
            result[key] = raw.removePrefix("\"").removeSuffix("\"").trim()
        }
        return result
    }

    private fun copyForkIcons(root: File) {
        val src = iconSourceDir.get().asFile
        if (!src.exists()) {
            logger.lifecycle("syncForkConfig: app-profile/icons/ not present — skipping icon copy (template defaults preserved)")
            return
        }

        val mappings = listOf(
            Triple("ios.png",             iosAppIconDir.get().asFile,      "AppIcon.png"),
            Triple("web-favicon.ico",     jsResourcesDir.get().asFile,     "favicon.ico"),
            Triple("web-favicon.ico",     wasmJsResourcesDir.get().asFile, "favicon.ico"),
            Triple("desktop-macos.icns",  desktopIconsDir.get().asFile,    "ic_launcher.icns"),
            Triple("desktop-windows.ico", desktopIconsDir.get().asFile,    "ic_launcher.ico"),
            Triple("desktop-linux.png",   desktopIconsDir.get().asFile,    "ic_launcher.png"),
        )
        var copied = 0
        for ((srcName, dstDir, dstName) in mappings) {
            val from = File(src, srcName)
            if (!from.exists()) continue
            dstDir.mkdirs()
            val to = File(dstDir, dstName)
            from.copyTo(to, overwrite = true)
            logger.lifecycle("syncForkConfig: copied app-profile/icons/$srcName → ${to.relativeTo(root)}")
            copied++
        }

        val androidSrc = File(src, "android/res")
        if (androidSrc.isDirectory && androidSrc.list()?.isNotEmpty() == true) {
            val androidDst = androidResDir.get().asFile
            androidSrc.copyRecursively(androidDst, overwrite = true)
            logger.lifecycle("syncForkConfig: copied app-profile/icons/android/res/ → ${androidDst.relativeTo(root)}/ (recursive)")
            copied++
        } else {
            logger.lifecycle("syncForkConfig: app-profile/icons/android/res/ not present — use Android Studio Image Asset Studio (one-time per fork, commit the result).")
        }

        if (copied == 0) {
            logger.lifecycle("syncForkConfig: app-profile/icons/ contained no recognised files — template defaults preserved")
        }
    }

    companion object {
        // Flat gradle/fork.properties key → dotted path in the merged app-profile map.
        // MIRRORS deployment/_shared/config.rb `AppProfile::MAP` KEY-FOR-KEY so the Ruby (fastlane)
        // and Kotlin (build) sides resolve the SAME dotted key to the SAME app-profile yaml path —
        // one white-label contract, two consumers. Keep the two in lockstep on any change.
        private val APP_PROFILE_MAP: Map<String, String> = mapOf(
            // ── identity / app ──
            "app.id" to "identity.app_id",
            "app.description" to "store.app_description",
            // ── org ──
            "org.name" to "org.name",
            "org.email" to "org.email",
            "org.first.name" to "org.first_name",
            "org.last.name" to "org.last_name",
            "org.phone" to "org.phone",
            "org.copyright" to "org.copyright",
            "org.marketing.url" to "org.marketing_url",
            "org.privacy.url" to "org.privacy_url",
            "org.support.url" to "org.support_url",
            // ── legal ──
            "legal.company.name" to "legal.company_name",
            "legal.jurisdiction" to "legal.jurisdiction",
            "legal.effective.date" to "legal.effective_date",
            "legal.contact.email" to "legal.contact_email",
            // ── keystore DN ──
            "keystore.dn.org_unit" to "keystore_dn.org_unit",
            "keystore.dn.city" to "keystore_dn.city",
            "keystore.dn.state" to "keystore_dn.state",
            "keystore.dn.country" to "keystore_dn.country",
            // ── store (common: iOS + macOS + Android) ──
            "store.primary.locale" to "store.primary_locale",
            "store.title" to "store.title",
            "store.subtitle" to "store.subtitle",
            "store.promotional.text" to "store.promotional_text",
            "store.release.notes" to "store.release_notes",
            "store.description" to "store.description",
            "store.copyright" to "store.copyright",
            "store.ios.age.rating" to "store.age_rating",
            "store.review.notes" to "store.review.notes",
            "store.review.demo.user" to "store.review.demo_user",
            "store.review.demo.password" to "store.review.demo_password",
            // ── android ──
            "store.android.category" to "android.category",
            "store.android.short.description" to "android.short_description",
            "store.android.changelog" to "android.changelog",
            "store.android.video.url" to "android.video_url",
            "android.play.tracks.by_flavor" to "android.play_tracks_by_flavor",
            "firebase.android.prod.app.id" to "android.firebase.app_id_prod",
            "firebase.android.demo.app.id" to "android.firebase.app_id_demo",
            "firebase.groups" to "android.firebase.groups",
            "play.testers.internal.googlegroup" to "android.play_testers.internal_googlegroup",
            "play.testers.closed.googlegroup" to "android.play_testers.closed_googlegroup",
            // ── apple (shared iOS + macOS) ──
            "apple.team.id" to "apple.team_id",
            "apple.match.git.url" to "apple.match.git.url",
            "apple.match.git.branch" to "apple.match.git.branch",
            "firebase.ios.prod.app.id" to "apple.firebase.ios_app_id_prod",
            "firebase.ios.demo.app.id" to "apple.firebase.ios_app_id_demo",
            "firebase.ios.app.id" to "apple.firebase.ios_app_id",
            "apple.testers.internal.group" to "apple.testers.internal_group",
            "apple.testers.external.group" to "apple.testers.external_group",
            "apple.testers.external.public.link" to "apple.testers.external_public_link",
            "store.ios.keywords" to "apple.keywords",
            "store.ios.category" to "apple.category",
            // ── apple / iOS-only ──
            "store.ios.secondary.category" to "apple.ios.secondary_category",
            "store.ios.apple.tv.privacy.url" to "apple.ios.apple_tv_privacy_url",
            // ── apple / macOS-only ──
            "store.macos.keywords" to "apple.macos.keywords",
            "store.macos.category" to "apple.macos.category",
            "store.macos.secondary.category" to "apple.macos.secondary_category",
            "mac.app.category" to "apple.macos.app_category",
            // ── web ──
            "web.cloudflare.project" to "web.cloudflare_project",
            // ── Windows / Microsoft Store (platforms/windows/windows.yaml, top-level `windows:`) ──
            "store.windows.ms.app.id" to "windows.ms_app_id",
            "store.windows.ms.publish.mode" to "windows.ms_publish_mode",
            "store.windows.ms.visibility" to "windows.ms_visibility",
            "windows.store.id" to "windows.store_id",
            "windows.msix.identity.name" to "windows.msix_identity_name",
            "windows.msix.identity.publisher" to "windows.msix_publisher",
            "windows.msix.publisher.display.name" to "windows.msix_publisher_display_name",
            "windows.partner.center.tenant.id" to "windows.partner_center.tenant_id",
            "windows.partner.center.client.id" to "windows.partner_center.client_id",
            // ── Ubuntu / Linux (platforms/ubuntu/ubuntu.yaml, top-level `ubuntu:`) ──
            "ubuntu.snap.name" to "ubuntu.snap.name",
            "ubuntu.snap.grade" to "ubuntu.snap.grade",
            "ubuntu.snap.confinement" to "ubuntu.snap.confinement",
            "ubuntu.snap.channel" to "ubuntu.snap.channel",
            "ubuntu.flathub.app.id" to "ubuntu.flathub.app_id",
            "ubuntu.deb.package" to "ubuntu.deb.package",
            "ubuntu.deb.section" to "ubuntu.deb.section",
            "ubuntu.deb.maintainer" to "ubuntu.deb.maintainer",
            "ubuntu.deb.homepage" to "ubuntu.deb.homepage",
            // ── apple / trade representative contact information ──
            "store.apple.trade.first.name" to "apple.trade_representative.first_name",
            "store.apple.trade.last.name" to "apple.trade_representative.last_name",
            "store.apple.trade.address.line1" to "apple.trade_representative.address_line1",
            "store.apple.trade.address.line2" to "apple.trade_representative.address_line2",
            "store.apple.trade.address.line3" to "apple.trade_representative.address_line3",
            "store.apple.trade.city" to "apple.trade_representative.city_name",
            "store.apple.trade.state" to "apple.trade_representative.state",
            "store.apple.trade.country" to "apple.trade_representative.country",
            "store.apple.trade.postal.code" to "apple.trade_representative.postal_code",
            "store.apple.trade.phone" to "apple.trade_representative.phone_number",
            "store.apple.trade.email" to "apple.trade_representative.email_address",
            "store.apple.trade.displayed" to "apple.trade_representative.is_displayed_on_app_store",
            // ── deploy-surface completeness (MS Store / winget / snap / flathub / aur / homebrew / ios-subcats / web) ──
            "win.store.name" to "windows.store.name",
            "win.store.short.description" to "windows.store.short_description",
            "win.store.description" to "windows.store.description",
            "win.store.category" to "windows.store.category",
            "win.store.privacy.url" to "windows.store.privacy_url",
            "win.winget.package.id" to "windows.winget.package_id",
            "win.winget.publisher" to "windows.winget.publisher",
            "win.winget.name" to "windows.winget.name",
            "win.winget.moniker" to "windows.winget.moniker",
            "ubuntu.snap.summary" to "ubuntu.snap.summary",
            "ubuntu.snap.description" to "ubuntu.snap.description",
            "ubuntu.flathub.developer" to "ubuntu.flathub.developer",
            "ubuntu.flathub.name" to "ubuntu.flathub.name",
            "ubuntu.flathub.summary" to "ubuntu.flathub.summary",
            "ubuntu.flathub.description" to "ubuntu.flathub.description",
            "ubuntu.aur.package.name" to "ubuntu.aur.package_name",
            "ubuntu.aur.source.type" to "ubuntu.aur.source_type",
            "mac.homebrew.cask.name" to "apple.macos.homebrew.cask_name",
            "mac.homebrew.tagline" to "apple.macos.homebrew.tagline",
            "store.ios.primary.first.subcategory" to "apple.ios.primary_first_sub_category",
            "store.ios.primary.second.subcategory" to "apple.ios.primary_second_sub_category",
            "store.ios.secondary.first.subcategory" to "apple.ios.secondary_first_sub_category",
            "store.ios.secondary.second.subcategory" to "apple.ios.secondary_second_sub_category",
            "web.custom.domain" to "web.custom_domain",
        )
    }
}
