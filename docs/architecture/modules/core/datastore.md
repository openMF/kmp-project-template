# `core/datastore`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATASTORE.md`
> **Measured:** 9 Kotlin files, 2 test files

## Principal types

`AppReviewPromptState`, `AppReviewPromptStore`, `ProjectPreferencesRepository`, `ProjectPreferencesRepositoryImpl`, `SettingsAppReviewPromptStore`, `UserPreferencesRepository`, `UserPreferencesRepositoryImpl`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/datastore sha=70a5f34bf469fd297d7686e083f25bd6c50c3b37 -->
## API reference

_Generated from `core/datastore` at tree `70a5f34bf469` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt`

```kotlin
val DatastoreModule = module
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:42</code></summary>

```kotlin
 */
val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    // Every repository's Koin binding, GENERATED from `@RepositoryBinding` on the implementation.
    // Emitted into this same package, so this file needs no import and keeps its zero-demo-reference
    // property — which is what lets a template sync blind-copy it. A stripped fork simply generates
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ProjectDatastoreModule.kt`

```kotlin
val ProjectDatastoreModule = module
```
THE FORK'S datastore DI seam. Empty on the neutral template — this is yours to fill. `DatastoreModule` beside it is `owner: template` and FULL-COPIES on a sync, so a binding added there is replaced on the next adopt.

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/AppReviewPromptStore.kt`

```kotlin
data class AppReviewPromptState(
```
What the review policy needs to know at app open, derived from `AppReviewPromptStore`.

```kotlin
interface AppReviewPromptStore
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:38</code></summary>

```kotlin
    // Review-prompt counters — Settings-backed, DEVICE-scoped (survives sign-out, unlike user prefs).
    // Read once per launch by the app shell, which asks AppReviewConfig whether they justify a prompt.
    single<AppReviewPromptStore> {
        SettingsAppReviewPromptStore(plainSettings = get<Settings>(named("plain")))
    }

    // Sync state persister — Settings-backed (same store as user prefs).
```

</details>

- `fun recordLaunch(): AppReviewPromptState` — Record an app launch and return the resulting state. Called once per launch from the app shell.
- `fun recordPromptShown()` — Stamp that a prompt was just requested, starting the cooldown. Recorded on REQUEST, not on a completed review, because neither Play nor StoreKit reports whether the user actually reviewed.

```kotlin
class SettingsAppReviewPromptStore(
```
`Settings`-backed `AppReviewPromptStore`, using the same plain store as user preferences.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:39</code></summary>

```kotlin
    // Read once per launch by the app shell, which asks AppReviewConfig whether they justify a prompt.
    single<AppReviewPromptStore> {
        SettingsAppReviewPromptStore(plainSettings = get<Settings>(named("plain")))
    }

    // Sync state persister — Settings-backed (same store as user prefs).
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ProjectPreferencesRepository.kt`

```kotlin
interface ProjectPreferencesRepository : UserPreferencesRepository
```
THE FORK'S preferences. Extends the framework's — this is yours to fill.

<details><summary>Example</summary>

```kotlin
interface ProjectPreferencesRepository : UserPreferencesRepository {
    val observeMyFlag: Flow<Boolean>
    suspend fun setMyFlag(enabled: Boolean)
}
override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
    analytics.log("theme_changed")
    delegate.setDarkThemeConfig(darkThemeConfig)
}
```

</details>

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ProjectPreferencesRepositoryImpl.kt:63</code></summary>

```kotlin
    val secureSettings: Settings,
    val dispatcher: DispatcherManager,
) : ProjectPreferencesRepository, UserPreferencesRepository by delegate
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ProjectPreferencesRepositoryImpl.kt`

```kotlin
class ProjectPreferencesRepositoryImpl(
```
Fork implementation of `ProjectPreferencesRepository`. `owner: fork` — never synced.

<details><summary>Example</summary>

```kotlin
override val observeMyFlag: Flow<Boolean> =
    MutableStateFlow(plainSettings.getBoolean(KEY_MY_FLAG, false))

override suspend fun setMyFlag(enabled: Boolean) = withContext(dispatcher.io) {
    plainSettings.putBoolean(KEY_MY_FLAG, enabled)
}
override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
    analytics.log("theme_changed")
    delegate.setDarkThemeConfig(darkThemeConfig)
}
```

</details>

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ProjectDatastoreModule.kt:32</code></summary>

```kotlin
val ProjectDatastoreModule = module {
    single<ProjectPreferencesRepository> {
        ProjectPreferencesRepositoryImpl(
            delegate = get<UserPreferencesRepository>(),
            // The SAME instances the framework's impl uses — an ownership boundary, not a second
            // store. Namespace fork keys so they cannot collide with a future framework preference.
            plainSettings = get<Settings>(named("plain")),
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/UserPreferencesRepository.kt`

```kotlin
interface UserPreferencesRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ProjectPreferencesRepository.kt:51</code></summary>

```kotlin
 * structural rather than a rule: drop one and this interface no longer satisfies its supertype.
 */
interface ProjectPreferencesRepository : UserPreferencesRepository
```

</details>

- `val userData: StateFlow<UserData>`
- `val authToken: String?` — The stored credential, or null when signed out. Synchronous read for a caller that already has one in hand; prefer `observeAuthToken` when the value can change under you.
- `val observeAuthToken: Flow<String?>` — The credential as a stream, re-emitting on sign-in and sign-out.
- `val passcode: String`
- `val observeLanguage: Flow<LanguageConfig>`
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>`
- `val observeDynamicColorPreference: Flow<Boolean>`
- `val observeScreenCapturePreference: Flow<Boolean>`
- `suspend fun setAuthToken(token: String?)` — Persist `token` into the ENCRYPTED store, or clear it when null. Call on sign-in with the value the auth endpoint returned (for Basic, the base64 of `user:password`; for OAuth, the access token) and on sign-out with null.
- `suspend fun setLanguage(language: LanguageConfig)`
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)`
- `suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)`
- `suspend fun setDynamicColorPreference(useDynamicColor: Boolean)`
- `suspend fun setIsAuthenticated(isAuthenticated: Boolean)`
  _…more members; read the file._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/UserPreferencesRepositoryImpl.kt`

```kotlin
class UserPreferencesRepositoryImpl(
```
Splits user data storage between plain (UI preferences) and secure (credentials/auth state) Settings backends.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:29</code></summary>

```kotlin

    single {
        UserPreferencesRepositoryImpl(
            plainSettings = get<Settings>(named("plain")),
            secureSettings = get<Settings>(named("secure")),
            dispatcher = get(),
        )
```

</details>

---

_7 type(s), 18 function(s)/property(ies); 12 carry KDoc at source; 2 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->
