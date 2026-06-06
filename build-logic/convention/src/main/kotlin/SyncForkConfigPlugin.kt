import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.work.DisableCachingByDefault
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
 *   - Platform app icons (from branding/icons/)
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
            iconBrandingDir.set(target.layout.projectDirectory.dir("branding/icons"))
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
    @get:Internal abstract val iconBrandingDir:    DirectoryProperty
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
            forkFile.inputStream().use(fork::load)
        } else {
            logger.warn(
                "syncForkConfig: gradle/fork.properties not found.\n" +
                "  Copy: cp gradle/fork.properties.template gradle/fork.properties\n" +
                "  Fill in your values, then re-run ./gradlew syncForkConfig"
            )
        }

        // ── 2. Load 6 build-time fields from gradle/libs.versions.toml ──────
        val toml = parseTomlVersions(File(root, "gradle/libs.versions.toml"))

        // Priority: ENV > fork.properties > TOML > ""
        fun get(forkKey: String, envKey: String? = null, tomlKey: String? = null): String {
            envKey?.let { System.getenv(it)?.takeIf(String::isNotBlank) }?.let { return it }
            (fork.getProperty(forkKey))?.takeIf(String::isNotBlank)?.let { return it }
            tomlKey?.let { toml[it]?.takeIf(String::isNotBlank) }?.let { return it }
            return ""
        }

        // Build-time fields (TOML-primary — always in the committed file)
        val appId          = get("app.id",           "APP_BUNDLE_ID",   "appId")
        val appDisplayName = get("app.display.name", "APP_DISPLAY_NAME","appDisplayName")
        val projectName    = get("project.name",     "PROJECT_NAME",    "projectName")

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
        val storeTitle        = get("store.title")
        val storeSubtitle     = get("store.subtitle")
        val storePromoText    = get("store.promotional.text")
        val storeReleaseNotes = get("store.release.notes")
        val storeCopyright    = get("store.copyright")
        val storeReviewNotes  = get("store.review.notes")

        // Store — Android
        val androidChangelog  = get("store.android.changelog")
        val androidShortDesc  = get("store.android.short.description")
        val androidCategory   = get("store.android.category")

        // Store — iOS
        val iosKeywords  = get("store.ios.keywords")
        val iosCategory  = get("store.ios.category")

        // Store — macOS
        val macosKeywords = get("store.macos.keywords")
        val macosCategory = get("store.macos.category")

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

        // ── 6. Store metadata files ───────────────────────────────────────────
        var written = 0

        fun writeIfPresent(rel: String, value: String) {
            if (value.isBlank()) return
            val f = File(root, rel)
            if (!f.parentFile.exists()) return
            f.writeText(value)
            logger.lifecycle("syncForkConfig: wrote $rel")
            written++
        }

        // iOS App Store
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/name.txt",             storeTitle)
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/subtitle.txt",         storeSubtitle)
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/keywords.txt",         iosKeywords)
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/promotional_text.txt", storePromoText)
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/release_notes.txt",    storeReleaseNotes)
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/marketing_url.txt",    marketingUrl)
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/privacy_url.txt",      privacyUrl)
        writeIfPresent("deployment/ios/appstore/metadata/en-GB/support_url.txt",      supportUrl)
        writeIfPresent("deployment/ios/appstore/metadata/copyright.txt",              storeCopyright)
        writeIfPresent("deployment/ios/appstore/metadata/primary_category.txt",       iosCategory)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/email_address.txt", orgEmail)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/first_name.txt",    orgFirstName)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/last_name.txt",     orgLastName)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/phone_number.txt",  orgPhone)
        writeIfPresent("deployment/ios/appstore/metadata/review_information/notes.txt",         storeReviewNotes)

        // macOS App Store
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/name.txt",             storeTitle)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/subtitle.txt",         storeSubtitle)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/keywords.txt",         macosKeywords)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/promotional_text.txt", storePromoText)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/release_notes.txt",    storeReleaseNotes)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/marketing_url.txt",    marketingUrl)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/privacy_url.txt",      privacyUrl)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/en-GB/support_url.txt",      supportUrl)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/copyright.txt",              storeCopyright)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/primary_category.txt",       macosCategory)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/email_address.txt", orgEmail)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/first_name.txt",    orgFirstName)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/last_name.txt",     orgLastName)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/phone_number.txt",  orgPhone)
        writeIfPresent("deployment/desktop/mac-app-store/metadata/review_information/notes.txt",         storeReviewNotes)

        // Android Play Store
        writeIfPresent("deployment/android/metadata/en-US/title.txt",             storeTitle)
        writeIfPresent("deployment/android/metadata/en-US/short_description.txt", androidShortDesc)
        writeIfPresent("deployment/android/metadata/en-US/changelogs/default.txt",androidChangelog)

        // full_description.txt — regex-replace the GitHub/support URL only (don't overwrite long-form copy)
        if (supportUrl.isNotBlank()) {
            val descFile = File(root, "deployment/android/metadata/en-US/full_description.txt")
            if (descFile.exists()) {
                val updated = descFile.readText().replace(Regex("GitHub: .*"), "GitHub: $supportUrl")
                descFile.writeText(updated)
                logger.lifecycle("syncForkConfig: updated deployment/android/metadata/en-US/full_description.txt")
            }
        }

        logger.lifecycle("syncForkConfig: wrote $written store metadata files")

        // ── 7. Fork icons ─────────────────────────────────────────────────────
        copyForkIcons(root)
    }

    private fun parseTomlVersions(toml: File): Map<String, String> {
        if (!toml.exists()) return emptyMap()
        val result = mutableMapOf<String, String>()
        toml.readLines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || !trimmed.contains("=")) return@forEach
            val eq = trimmed.indexOf('=')
            val key = trimmed.substring(0, eq).trim()
            val raw = trimmed.substring(eq + 1).trim().removePrefix("\"").removeSuffix("\"").trim()
            // Drop inline TOML comment
            result[key] = raw.substringBefore(" #").trim()
        }
        return result
    }

    private fun copyForkIcons(root: File) {
        val src = iconBrandingDir.get().asFile
        if (!src.exists()) {
            logger.lifecycle("syncForkConfig: branding/icons/ not present — skipping icon copy (template defaults preserved)")
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
            logger.lifecycle("syncForkConfig: copied branding/icons/$srcName → ${to.relativeTo(root)}")
            copied++
        }

        val androidSrc = File(src, "android")
        if (androidSrc.isDirectory && androidSrc.list()?.isNotEmpty() == true) {
            val androidDst = androidResDir.get().asFile
            androidSrc.copyRecursively(androidDst, overwrite = true)
            logger.lifecycle("syncForkConfig: copied branding/icons/android/ → ${androidDst.relativeTo(root)}/ (recursive)")
            copied++
        } else {
            logger.lifecycle("syncForkConfig: branding/icons/android/ not present — use Android Studio Image Asset Studio (one-time per fork, commit the result).")
        }

        if (copied == 0) {
            logger.lifecycle("syncForkConfig: branding/icons/ contained no recognised files — template defaults preserved")
        }
    }
}
