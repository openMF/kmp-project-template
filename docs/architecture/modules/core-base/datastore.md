# `core-base/datastore`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_DATASTORE.md`
> **Measured:** 17 Kotlin files, 2 test files

## Principal types

`BrowserStorage`, `CachedSecureSettings`, `ChangeListVersions`, `SecureBlobStorage`, `SecureCipher`, `SecureSettingsFactory`, `SecureStoreCore`, `SettingsSyncStatePersister`, `SyncStatePersister`, `WebCryptoCipher`, `WebSecureStore`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/datastore sha=34a6943c1e27a12f5e4918eb83a21f79278cb9c6 -->
## API reference

_Generated from `core-base/datastore` at tree `34a6943c1e27` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/datastore/src/commonMain/kotlin/kpt/core/base/datastore/di/DatastoreBaseModule.kt`

```kotlin
expect val datastoreBasePlatformModule: Module
```
Platform-specific module that provides `SecureSettingsFactory`. Android needs Context; other platforms use no-arg constructors.

```kotlin
val DatastoreBaseModule = module
```
Provides two `Settings` instances via Koin named qualifiers: - `named("plain")`: Standard unencrypted settings - `named("secure")`: Encrypted settings backed by platform secure storage

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:26</code></summary>

```kotlin

val DatastoreModule = module {
    includes(CommonModule, DatastoreBaseModule)

    single {
        UserPreferencesRepositoryImpl(
            plainSettings = get<Settings>(named("plain")),
```

</details>

### `core-base/datastore/src/commonMain/kotlin/kpt/core/base/datastore/infra/ChangeListVersions.kt`

```kotlin
data class ChangeListVersions(val versions: Map<String, Long> = emptyMap())
```
Per-feature last-synced version map.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/infra/ChangeListVersionsTest.kt:26</code></summary>

```kotlin
    @Test
    fun round_trips_through_kotlinx_serialization() {
        val original = ChangeListVersions(
            mapOf("currency-rates" to 1_700_000_000L, "macro-indicators" to 42L),
        )
        val encoded = Json.encodeToString(ChangeListVersions.serializer(), original)
        val decoded = Json.decodeFromString(ChangeListVersions.serializer(), encoded)
```

</details>

### `core-base/datastore/src/commonMain/kotlin/kpt/core/base/datastore/infra/SyncStatePersister.kt`

```kotlin
interface SyncStatePersister
```
Persistence seam for the `Synchronizer`'s per-feature last-synced version map. Backed by Multiplatform Settings (same store used by `UserPreferencesRepositoryImpl` for plain user prefs) — survives process restart but not data wipe.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:44</code></summary>

```kotlin
    // Sync state persister — Settings-backed (same store as user prefs).
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
    single<SyncStatePersister> {
        SettingsSyncStatePersister(plainSettings = get<Settings>(named("plain")))
    }
}
```

</details>

- `suspend fun read(): ChangeListVersions`
- `suspend fun write(versions: ChangeListVersions)`

```kotlin
class SettingsSyncStatePersister(
```
`SyncStatePersister` backed by multiplatform-settings — the per-feature last-synced version map, serialised under a single key.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:45</code></summary>

```kotlin
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
    single<SyncStatePersister> {
        SettingsSyncStatePersister(plainSettings = get<Settings>(named("plain")))
    }
}
```

</details>

### `core-base/datastore/src/commonMain/kotlin/kpt/core/base/datastore/SecureSettingsFactory.kt`

```kotlin
expect class SecureSettingsFactory
```
Platform-specific factory that creates an encrypted `Settings` instance. Returns the standard `Settings` interface — zero API change for consumers.

---

_4 type(s), 4 function(s)/property(ies); 6 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
