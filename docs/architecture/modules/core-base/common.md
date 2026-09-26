# `core-base/common`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_COMMON.md`
> **Measured:** 10 Kotlin files, 0 test files

**Defines annotations:** `@IgnoredOnParcel`, `@Parcelize`, `@TypeParceler`

## Principal types

`DispatcherManager`, `DispatcherManagerImpl`, `IgnoredOnParcel`, `Parcel`, `Parcelable`, `Parceler`, `Parcelize`, `TypeParceler`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/common sha=0d96434ddf63a007186e6db82c6514c62a1a150a -->
## API reference

_Generated from `core-base/common` at tree `0d96434ddf63` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/di/CommonModule.kt`

```kotlin
val CommonModule = module
```
Koin module for `core-base/common` — currently the platform `dispatcherManagerModule` binding. Include it once from the app's module graph; every other core module assumes a `DispatcherManager` is already resolvable.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:26</code></summary>

```kotlin

val DatastoreModule = module {
    includes(CommonModule, DatastoreBaseModule)

    single {
        UserPreferencesRepositoryImpl(
            plainSettings = get<Settings>(named("plain")),
```

</details>

```kotlin
expect val dispatcherManagerModule: Module
```
The per-platform `DispatcherManager` binding, supplied by each target's `actual`. Separate from `CommonModule` because the dispatcher set is the one part of this module that cannot be expressed in common code.

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/ImageExtension.kt`

```kotlin
fun ByteArray.toBase64(): String
```
Extension function to convert ByteArray to Base64 string

```kotlin
fun ByteArray.toBase64DataUri(mimeType: String = "application/octet-stream"): String
```
Extension function to convert ByteArray to Base64 string with data URI prefix

```kotlin
fun String.fromBase64(): ByteArray
```
Extension function to convert Base64 string to ByteArray

```kotlin
fun String.fromBase64OrNull(): ByteArray?
```
Extension function to safely convert Base64 string to ByteArray

```kotlin
fun String.fromBase64DataUri(): ByteArray
```
Extension function to convert Base64 data URI to ByteArray Handles data URIs in format: "data:mime/type;base64,actualBase64Data"

```kotlin
fun String.fromBase64DataUriOrNull(): ByteArray?
```
Extension function to safely convert Base64 data URI to ByteArray

```kotlin
fun String.extractMimeTypeFromDataUri(): String?
```
Extension function to extract MIME type from Base64 data URI

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/manager/DispatcherManager.kt`

```kotlin
interface DispatcherManager
```
Injectable access to the app's coroutine dispatchers.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ProjectPreferencesRepositoryImpl.kt:62</code></summary>

```kotlin
    val plainSettings: Settings,
    val secureSettings: Settings,
    val dispatcher: DispatcherManager,
) : ProjectPreferencesRepository, UserPreferencesRepository by delegate
```

</details>

- `val default: CoroutineDispatcher` — The default `CoroutineDispatcher` for the app.
- `val main: MainCoroutineDispatcher` — The `MainCoroutineDispatcher` for the app.
- `val io: CoroutineDispatcher` — The IO `CoroutineDispatcher` for the app.
- `val unconfined: CoroutineDispatcher` — The unconfined `CoroutineDispatcher` for the app.
- `val appScope: CoroutineScope`

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/Parcelize.kt`

```kotlin
expect annotation class Parcelize()
```
Marks a class as parcelable so it can cross an Android process/configuration boundary.

```kotlin
expect interface Parcelable
```
The platform's parcelable contract — `android.os.Parcelable` on Android, an empty marker elsewhere. Implement it on a shared model together with `Parcelize`; do not hand-write the read/write pair.

```kotlin
expect annotation class IgnoredOnParcel()
```
Excludes one property from parcelling, for values that are derived or cannot cross a process boundary (a lambda, a coroutine scope, a cached bitmap). The property must have a default, since it is reconstructed rather than restored.

```kotlin
expect interface Parceler<P>
```
Custom parcelling for a type the platform cannot serialise on its own — a value class, a third-party type, anything needing a narrower wire form than its fields. Pair it with `TypeParceler` at the use site.

- `fun create(parcel: Parcel): P`
- `fun P.write(parcel: Parcel, flags: Int)`

```kotlin
expect annotation class TypeParceler<T, P : Parceler<in T>>()
```
Binds a `Parceler` to type `T` for one property or file, so a shared model can carry a type the platform does not know how to parcel.

```kotlin
expect class Parcel
```
The platform write buffer a `Parceler` reads from and writes to. Strictly POSITIONAL — there are no field names on the wire, so reads must mirror the write order exactly.

---

_7 type(s), 16 function(s)/property(ies); 20 carry KDoc at source; 0 authored example(s); 2 live call site(s)._
<!-- api-docs:end -->
