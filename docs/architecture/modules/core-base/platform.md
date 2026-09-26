# `core-base/platform`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_PLATFORM.md`
> **Measured:** 23 Kotlin files, 0 test files

## Principal types

`AppContext`, `AppReviewManager`, `AppReviewManagerImpl`, `AppUpdateManager`, `AppUpdateManagerImpl`, `GarbageCollectionManager`, `GarbageCollectionManagerImpl`, `IntentManager`, `IntentManagerImpl`, `MimeType`, `ShareManager`, `ShareManagerImpl`, `UpdateOutcome`, `UrlLauncher`  …and 1 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/platform sha=b3ca4cb2c7a44b3a7b6d2584197b085794bc6e55 -->
## API reference

_Generated from `core-base/platform` at tree `b3ca4cb2c7a4` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/context/AppContext.kt`

```kotlin
expect abstract class AppContext
```
Represents an abstract context for the application that provides platform-specific functionality. This class must be implemented in each platform-specific source set.

```kotlin
expect val LocalContext: ProvidableCompositionLocal<AppContext>
```
A composition local that provides the current `AppContext` to the composition tree. This allows composable functions to access the platform-specific context without explicit parameters.

<details><summary>Used in the template — <code>core/designsystem/src/androidMain/kotlin/kpt/core/designsystem/theme/Theme.android.kt:23</code></summary>

```kotlin
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        useDarkTheme -> darkScheme
```

</details>

```kotlin
expect val AppContext.activity: Any
```
The platform-specific activity or view controller associated with the current context. This property is accessible only from within a Composable function.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/di/PlatformModule.kt`

```kotlin
val platformModule = module
```
Koin module binding every `core-base/platform` capability — toast, share, URL launch, app update, garbage-collection hint — plus the per-target bindings from each `actual`. Include it once from the app graph.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/garbage/GarbageCollectionManager.kt`

```kotlin
interface GarbageCollectionManager
```
A hint to the platform that now is a reasonable moment to reclaim memory. A HINT, never a guarantee — no runtime here promises to collect on request.

- `fun tryCollect()` — Calls the garbage collector on the `Runtime` in an effort to clear the unused resources in the heap.

```kotlin
expect val garbageCollector: () -> Unit
```
The platform's collection hint, bound per target — `Runtime.getRuntime().gc()` on the JVM, a no-op where the runtime exposes no such control. Prefer the injectable `GarbageCollectionManager` at call sites; this exists for its `actual`s.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/garbage/GarbageCollectionManagerImpl.kt`

```kotlin
class GarbageCollectionManagerImpl(
```
Default `GarbageCollectionManager`: forwards to the platform `garbageCollector` on `dispatcher`, keeping a single in-flight job so repeated calls coalesce instead of queueing pauses.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/intent/IntentManager.kt`

```kotlin
interface IntentManager
```
Asks the OS for a specific system screen or document flow, and reports what came back. ## Scope — system intents ONLY This interface used to also carry `launchUri` and the `share*` family, which conflated three different acts.

- `suspend fun openAppSettings(): IntentResult` — Open this app's entry in system settings — the target for a denied-permission rationale.
- `suspend fun createDocument(fileName: String, mimeType: String = "*/*"): IntentResult` — Ask the OS for a save location, returning the chosen document's uri.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/intent/IntentManagerImpl.kt`

```kotlin
class IntentManagerImpl : IntentManager
```
The one `IntentManager`, for every target. `cmp-intent-launcher` carries the per-target `actual`s, so there is no source-set split here.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/LocalManagerProviders.kt`

```kotlin
fun LocalManagerProvider(
```
Provides every platform manager to composition. This was an `expect fun` with androidMain and nonAndroidMain actuals.

```kotlin
val LocalAppReviewManager: ProvidableCompositionLocal<AppReviewManager> =
```
Prompts for an app-store review. Provided by `LocalManagerProvider`.

```kotlin
val LocalIntentManager: ProvidableCompositionLocal<IntentManager> =
```
Launches platform intents — view, pick, open settings. Provided by `LocalManagerProvider`.

```kotlin
val LocalUrlLauncher: ProvidableCompositionLocal<UrlLauncher> =
```
Opens URLs, email, maps, phone and SMS. Provided by `LocalManagerProvider`.

```kotlin
val LocalShareManager: ProvidableCompositionLocal<ShareManager> =
```
Shares text, URLs, files and images to other apps. Provided by `LocalManagerProvider`.

```kotlin
val LocalAppUpdateManager: ProvidableCompositionLocal<AppUpdateManager> =
```
Checks for and starts app updates. Provided by `LocalManagerProvider`.

```kotlin
val LocalClipboardManager: ProvidableCompositionLocal<ClipboardManager> =
```
Reads and writes the system clipboard, plus history, change observation and URL detection. Typed as the toolkit's `ClipboardManager` rather than a template wrapper — see the note in `platformModule`.

```kotlin
val LocalBubbleManager: ProvidableCompositionLocal<Bubble> =
```
Shows floating bubbles, overlays and heads-up UI. Provided by `LocalManagerProvider`.

```kotlin
val LocalPdfManager: ProvidableCompositionLocal<PdfManager> =
```
Generates PDFs — statements, receipts, invoices. Provided by `LocalManagerProvider`.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/model/MimeType.kt`

```kotlin
enum class MimeType(val value: String, vararg val extensions: String)
```
Represents standardized MIME (Multipurpose Internet Mail Extensions) types for various file formats.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/review/AppReviewManager.kt`

```kotlin
interface AppReviewManager
```
Manages application review requests across platforms. This interface abstracts the platform-specific implementations for requesting user reviews of the application.

- `val capabilities: AppReviewCapabilities` — What review means on THIS target: `nativeInAppReview`, `storeListing`, and the derived `canRequestReview`.
- `val canRequestReview: Boolean` — Whether a review can actually be requested on THIS target right now. Ask before rendering a "Rate this app" affordance.
- `suspend fun promptForReview()` — Prompts the user to review the app using the platform's standard review flow. This method triggers the native system review prompt, which is typically managed by the platform to control frequency and prevent review fatigue.
- `fun promptForCustomReview()` — Prompts the user to review the app using a custom application-defined review flow.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/review/AppReviewManagerImpl.kt`

```kotlin
class AppReviewManagerImpl : AppReviewManager
```
The single implementation of `AppReviewManager`, for every target. Replaces the previous androidMain/nonAndroidMain pair.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/share/ShareManager.kt`

```kotlin
interface ShareManager
```
Hands content to the platform share chooser.

- `suspend fun shareText(text: String)` — Share plain text.
- `suspend fun shareUrl(url: String)` — Share a URL as a link rather than as text, so receivers render a preview.
- `suspend fun shareFile(fileUri: String, mimeType: MimeType)` — Share a file by uri. `mimeType` decides which apps the chooser offers.
- `suspend fun shareFile(fileUri: String, mimeType: MimeType, extraText: String)` — Share a file alongside a message — one chooser, both payloads.
- `suspend fun shareImage(title: String, image: ImageBitmap)` — Share an in-memory image. Encoded to PNG on the way out; `title` names the file.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/share/ShareManagerImpl.kt`

```kotlin
class ShareManagerImpl : ShareManager
```
The one `ShareManager`, for every target. `cmp-share` carries the per-target `actual`s.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/update/AppUpdateManager.kt`

```kotlin
interface AppUpdateManager
```
In-app update check, with real behaviour on every target.

- `suspend fun checkForAppUpdate(): UpdateOutcome` — Check for an update and, if one is available, start the flow.
- `suspend fun checkForResumeUpdateState(): UpdateOutcome` — Re-check after the app returns to the foreground, so an update the user backgrounded mid-flow is offered again. Call from the host's resume hook.
- `fun isSupported(): Boolean` — Whether this target can perform an in-app update at all.

```kotlin
sealed interface UpdateOutcome
```
What an update check concluded. Flattened from the engine's richer result type.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/update/AppUpdateManagerImpl.kt`

```kotlin
class AppUpdateManagerImpl(
```
The one `AppUpdateManager`, for every target. There is deliberately no `androidMain` / `nonAndroidMain` split: `cmp-in-app-update` carries the per-target `actual`s.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/url/UrlLauncher.kt`

```kotlin
interface UrlLauncher
```
Opens a URL in whatever the platform considers the right handler.

- `fun open(url: String): Boolean` — Open `url` with the platform's default handler. Returns false if nothing could handle it.
- `fun openInBrowser(url: String): Boolean` — Open `url` in a browser specifically, bypassing any app that claims the link.
- `fun canOpen(url: String): Boolean` — Whether `url` has a handler — check before offering the action, not after it fails.

### `core-base/platform/src/commonMain/kotlin/kpt/core/base/platform/url/UrlLauncherImpl.kt`

```kotlin
class UrlLauncherImpl : UrlLauncher
```
The one `UrlLauncher`, for every target. `cmp-open-url` carries the per-target `actual`s, so there is no source-set split here.

---

_15 type(s), 31 function(s)/property(ies); 46 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->
