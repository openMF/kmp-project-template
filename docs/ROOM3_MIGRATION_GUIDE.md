# Room 2 to Room 3 Migration Guide (Consumer Projects)

**Applies to**: `mifos-pay`, `mifos-mobile`, `mifos-x-field-officer-app`, and any project consuming `kmp-project-template` via `sync-dirs.sh`

**Last Updated**: 2026-04-30

> **IMPORTANT: Kotlin Version Requirement**
> Room 3.0-alpha03 klibs are compiled with Kotlin 2.3.20. You MUST upgrade Kotlin, KSP, and Compose Multiplatform:
> | Dependency | Required Version |
> |-----------|-----------------|
> | Kotlin | **2.3.20** |
> | KSP | **2.3.6** (KSP2, decoupled from Kotlin version) |
> | Compose Multiplatform | **1.10.3** |
>
> Update these in `gradle/libs.versions.toml` BEFORE starting the migration steps below.

---

## What You Get For Free (Auto-Synced)

After the template merges Room 3 changes to `dev`, the next `sync-dirs.sh` run (weekly cron Monday midnight OR manual dispatch) automatically propagates:

| Directory | Contains | Action Required |
|-----------|----------|:---------------:|
| `core-base/database/` | Room 3 `AppDatabaseFactory` (all platforms), deleted abstraction layer | None |
| `build-logic/convention/` | `KMPRoomConventionPlugin` with Room 3 plugin + `kspJs`/`kspWasmJs` | None |
| `cmp-desktop/` | Desktop config | None |
| `cmp-web/` | Web module config | None |
| `.github/` | CI workflows | None |

## What You Must Migrate Manually (7 Steps)

These directories are NOT synced - each consumer owns them:

| Not Synced | Why |
|-----------|-----|
| `gradle/libs.versions.toml` | Project-specific dependency versions |
| `core/database/` | Project-specific entities, DAOs, DI, AppDatabase |
| `cmp-android/proguard-rules.pro` | Project-specific ProGuard |
| Tests (`*Test/`, `*UnitTest/`) | Project-specific test code |

**Estimated time**: 2-4 hours (mostly mechanical find-and-replace + delete)

---

## Step 1: Version Catalog (`gradle/libs.versions.toml`)

Update Room coordinates from v2 to v3.

```toml
# ---- BEFORE (Room 2.8.4) ----
[versions]
room = "2.8.4"

[libraries]
androidx-room-gradle-plugin = { module = "androidx.room:room-gradle-plugin", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }

[plugins]
room = { id = "androidx.room", version.ref = "room" }

# ---- AFTER (Room 3.0) ----
[versions]
room = "3.0.0-alpha03"

[libraries]
androidx-room-gradle-plugin = { module = "androidx.room3:room3-gradle-plugin", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room3:room3-compiler", version.ref = "room" }
androidx-room-runtime = { module = "androidx.room3:room3-runtime", version.ref = "room" }
# DELETED: androidx-room-ktx (merged into room3-runtime)

[plugins]
room = { id = "androidx.room3", version.ref = "room" }
```

**Key changes**:
- Maven group: `androidx.room` -> `androidx.room3`
- Artifact names: `room-*` -> `room3-*`
- Plugin ID: `androidx.room` -> `androidx.room3`
- `room-ktx` is gone (merged into `room3-runtime`)

**Verify**: `./gradlew :build-logic:convention:build` - convention plugin must resolve new artifacts.

---

## Step 2: Import Migration (`core/database/src/**/*.kt`)

Replace ALL `template.core.base.database.*` imports with `androidx.room3.*`.

The old `core-base/database` abstraction layer re-exported Room annotations under the `template.core.base.database` package. Room 3 annotations work directly in `commonMain` - use them directly.

**One-liner** (run from project root):
```bash
find core/database/src -name "*.kt" -exec sed -i '' 's/import template\.core\.base\.database\./import androidx.room3./g' {} +
```

**Complete import mapping**:

| Old (via abstraction) | New (Room 3 direct) |
|----------------------|---------------------|
| `template.core.base.database.Dao` | `androidx.room3.Dao` |
| `template.core.base.database.Query` | `androidx.room3.Query` |
| `template.core.base.database.Insert` | `androidx.room3.Insert` |
| `template.core.base.database.Update` | `androidx.room3.Update` |
| `template.core.base.database.Delete` | `androidx.room3.Delete` |
| `template.core.base.database.Upsert` | `androidx.room3.Upsert` |
| `template.core.base.database.Transaction` | `androidx.room3.Transaction` |
| `template.core.base.database.Entity` | `androidx.room3.Entity` |
| `template.core.base.database.PrimaryKey` | `androidx.room3.PrimaryKey` |
| `template.core.base.database.ColumnInfo` | `androidx.room3.ColumnInfo` |
| `template.core.base.database.Embedded` | `androidx.room3.Embedded` |
| `template.core.base.database.Relation` | `androidx.room3.Relation` |
| `template.core.base.database.ForeignKey` | `androidx.room3.ForeignKey` |
| `template.core.base.database.Index` | `androidx.room3.Index` |
| `template.core.base.database.Ignore` | `androidx.room3.Ignore` |
| `template.core.base.database.Database` | `androidx.room3.Database` |
| `template.core.base.database.DatabaseView` | `androidx.room3.DatabaseView` |
| `template.core.base.database.TypeConverter` | `androidx.room3.TypeConverter` |
| `template.core.base.database.TypeConverters` | `androidx.room3.TypeConverters` |
| `template.core.base.database.AutoMigration` | `androidx.room3.AutoMigration` |
| `template.core.base.database.OnConflictStrategy` | `androidx.room3.OnConflictStrategy` |
| `template.core.base.database.RoomDatabase` | `androidx.room3.RoomDatabase` |
| `template.core.base.database.ConstructedBy` | `androidx.room3.ConstructedBy` (still required) |
| `template.core.base.database.RoomDatabaseConstructor` | `androidx.room3.RoomDatabaseConstructor` (still required) |

Also replace any direct Room 2 imports:
```bash
find core/database/src -name "*.kt" -exec sed -i '' 's/import androidx\.room\./import androidx.room3./g' {} +
```

**Verify**: `grep -r "template\.core\.base\.database" core/database/` should return zero matches.

---

## Step 3: Unify AppDatabase to commonMain

Consumer `AppDatabase` uses the expect/actual pattern (like the template did). Move to a single commonMain class.

### 3a. Rewrite `core/database/src/commonMain/.../AppDatabase.kt`

```kotlin
// ---- BEFORE (expect class, no annotations) ----
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect abstract class AppDatabase {
    abstract val chargeDao: ChargeDao
    abstract val notificationDao: MifosNotificationDao
    // ... other DAOs
}

// ---- AFTER (concrete class, fully annotated) ----
package org.mifos.core.database  // keep your existing package

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.TypeConverters

// Required for KSP to generate AppDatabase_Impl on all platforms
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

@Database(
    entities = [
        ChargeEntity::class,
        MifosNotificationEntity::class,
        // ... ALL your entities (copy from old platform actuals)
    ],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(YourTypeConverters::class)  // if you have type converters
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val chargeDao: ChargeDao
    abstract val notificationDao: MifosNotificationDao
    // ... other DAOs (same as before, just no 'expect')

    companion object {
        const val VERSION = 1  // keep your current version
        const val DATABASE_NAME = "your_database.db"  // keep your current name
    }
}
```

**Key changes**:
- Remove `expect` keyword from `AppDatabase` class
- Add `expect object AppDatabaseConstructor` (KSP generates the actual on each platform)
- Add `@Database(entities = [...])` - copy entity list from your Android/Desktop actual
- Add `@TypeConverters(...)` - copy from your Android/Desktop actual
- Add `@ConstructedBy(AppDatabaseConstructor::class)`
- Add `: RoomDatabase()` superclass
- Add `companion object` with VERSION + DATABASE_NAME

### 3b. DELETE platform-specific AppDatabase actuals

```bash
rm core/database/src/androidMain/kotlin/.../AppDatabase.android.kt
rm core/database/src/desktopMain/kotlin/.../AppDatabase.desktop.kt
rm core/database/src/nativeMain/kotlin/.../AppDatabase.native.kt
```

**NOTE**: The `expect object AppDatabaseConstructor` now lives in `commonMain` (not in platform actuals). KSP generates the `actual object` on each platform automatically. Delete only the platform-specific `AppDatabase.*.kt` files.

---

## Step 4: Simplify DI Modules

Platform DI modules should create the database using `AppDatabaseFactory` + `BundledSQLiteDriver`.

### Android (`DatabaseModule.android.kt`):
```kotlin
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import template.core.base.database.AppDatabaseFactory

actual val platformModule: Module = module {
    single {
        AppDatabaseFactory(androidApplication())
            .createDatabase<AppDatabase>(
                databaseName = AppDatabase.DATABASE_NAME,
            )
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
```

### Desktop (`DatabaseModule.desktop.kt`):
```kotlin
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import template.core.base.database.AppDatabaseFactory

actual val platformModule: Module = module {
    single {
        AppDatabaseFactory()
            .createDatabase<AppDatabase>(
                databaseName = AppDatabase.DATABASE_NAME,
            )
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
```

### Native/iOS (`DatabaseModule.native.kt`):
```kotlin
// Same as Desktop (AppDatabaseFactory handles iOS document directory internally)
```

### JS (`DatabaseModule.js.kt`) - was EMPTY, now functional:
```kotlin
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import template.core.base.database.AppDatabaseFactory

actual val platformModule: Module = module {
    single {
        AppDatabaseFactory()
            .createDatabase<AppDatabase>(
                databaseName = AppDatabase.DATABASE_NAME,
            )
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }
}
```

### WasmJS (`DatabaseModule.wasmJs.kt`):
```kotlin
// Same as JS
```

**Key changes across all platforms**:
- `BundledSQLiteDriver()` is mandatory on Android/Desktop/Native (Room 3 has no implicit default)
- `setQueryCoroutineContext()` takes `CoroutineDispatcher` directly (no `as CoroutineContext` cast needed)
- JS/WasmJS now have real database creation (were empty stubs)
- Removed `AppDispatchers` named qualifier lookup - use `Dispatchers.IO` directly

---

## Step 5: Update `core/database/build.gradle.kts`

Remove redundant Room dependencies. The convention plugin + `core-base` handle everything.

```kotlin
// REMOVE these blocks (convention plugin adds room-runtime):
// androidMain.dependencies { implementation(libs.androidx.room.runtime) }
// desktopMain.dependencies { implementation(libs.androidx.room.runtime) }
// nativeMain.dependencies { implementation(libs.androidx.room.runtime) }
// desktopMain.dependencies { implementation(libs.androidx.sqlite.bundled) }
// nativeMain.dependencies { implementation(libs.androidx.sqlite.bundled) }
// val desktopMain by getting { ... }  (Gradle anti-pattern)

// KEEP:
commonMain.dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    api(projects.core.common)
    api(projects.coreBase.database)  // brings Room 3 runtime transitively
}
androidMain.dependencies {
    implementation(libs.koin.android)
}
```

---

## Step 6: Update Tests

### 6a. Update existing test imports

```bash
find core/database/src -path "*/test*" -name "*.kt" -exec sed -i '' \
  's/import androidx\.room\.Room/import androidx.room3.Room/g' {} +
```

Test DI modules - update to Room 3 context-free in-memory builder:
```kotlin
// BEFORE (Room 2)
Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)

// AFTER (Room 3 - context-free, reified)
Room.inMemoryDatabaseBuilder<AppDatabase>()
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
```

### 6b. CREATE web test modules (NEW)

```kotlin
// core/database/src/jsTest/kotlin/.../di/TestDatabaseModule.js.kt
actual val testPlatformModule: Module = module {
    factory<AppDatabase> {
        Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }
}

// core/database/src/wasmJsTest/kotlin/.../di/TestDatabaseModule.wasmJs.kt
actual val testPlatformModule: Module = module {
    factory<AppDatabase> {
        Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }
}
```

### 6c. Remove `@Suppress("NO_ACTUAL_FOR_EXPECT")` from TestDatabaseModule.kt (commonTest)

---

## Step 7: ProGuard + Verification

### 7a. Update ProGuard rules (if project has custom Room rules)
```proguard
# BEFORE:
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# AFTER:
-keep class * extends androidx.room3.RoomDatabase { <init>(); }
```

### 7b. Wire DatabaseModule (if not already wired)

Check `cmp-navigation/.../di/KoinModules.kt` - ensure `DatabaseModule` is in `allModules`:
```kotlin
import org.mifos.core.database.di.DatabaseModule

object KoinModules {
    val allModules = listOf(
        // ... existing modules ...
        DatabaseModule,  // ADD if missing
    )
}
```

Also add `core:database` to `cmp-navigation/build.gradle.kts` dependencies if not present:
```kotlin
commonMain.dependencies {
    implementation(projects.core.database)
}
```

### 7c. Verification checklist

Run these from your project root:

```bash
./gradlew :core:database:build                     # compiles all platforms
./gradlew :core:database:allTests                   # all tests pass
./gradlew :cmp-android:assembleDebug                # Android app builds
./gradlew :cmp-desktop:run                          # Desktop app launches

# Zero-match checks:
grep -r "template\.core\.base\.database" core/      # ZERO matches
grep -r "import androidx\.room\." core/ | grep -v room3  # ZERO matches
grep -r "findAndInstantiateDatabaseImpl" core/      # ZERO matches
grep -r "findDatabaseConstructorAndInitDatabaseImpl" core/  # ZERO matches
grep -r "RoomDatabaseConstructor" core/             # ZERO matches
grep -r "NO_ACTUAL_FOR_EXPECT" core/database/       # ZERO matches
```

Room 3 reads Room 2 database schemas - existing data is preserved with no migration needed.

---

## Per-Consumer Notes

### mifos-mobile
- Has more entities than template (ChargeEntity, MifosNotificationEntity, ClientEntity, etc.)
- Copy ALL entity classes from `@Database(entities = [...])` in the old Android actual to new commonMain AppDatabase
- Has custom TypeConverters - update imports

### mifos-pay
- Has payment-specific entities (TransactionEntity, etc.)
- Same migration pattern - unify AppDatabase to commonMain
- Ensure all DAOs are listed in commonMain DatabaseModule

### mifos-x-field-officer-app
- Largest entity count (LoanEntity, CenterEntity, GroupEntity, etc.)
- Take extra care copying entity list to commonMain
- If you have Room migrations (version > 1), update migration callbacks: `androidx.room` -> `androidx.room3`

---

## Quick Reference

| What | Room 2 | Room 3 |
|------|--------|--------|
| Maven group | `androidx.room` | `androidx.room3` |
| Gradle plugin | `androidx.room` | `androidx.room3` |
| Config block | `room { }` | `room3 { }` |
| Package | `androidx.room.*` | `androidx.room3.*` |
| KSP targets | Android, Desktop, Native | Android, Desktop, Native, **JS, WasmJS** |
| `room-ktx` | Separate artifact | Merged into `room3-runtime` |
| SQLiteDriver | Optional (Android default) | **Mandatory** on all platforms |
| `inMemoryDatabaseBuilder` | Needs `Context` on Android | Context-free (reified) |
| `@ConstructedBy` | Required on Native | Still required (KSP generates actual) |
| `room.generateKotlin` KSP arg | Required | Removed (always Kotlin) |
| Web support | Stub only | Full (OPFS persistence) |
