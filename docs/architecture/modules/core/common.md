# `core/common`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_COMMON.md`
> **Measured:** 3 Kotlin files, 0 test files

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/common sha=85bebc59fe537ea1e4350abc62d05c8b0edfefc8 -->
## API reference

_Generated from `core/common` at tree `85bebc59fe53` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatDate.kt`

```kotlin
fun formatDate(millis: Long): String
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatDuration.kt`

```kotlin
fun formatTimeAgo(instant: Instant?): String?
```
Returns a human-readable "X ago" label for a past `instant`, e.g. "just now", "5m ago", "2h ago", "3d ago". Returns `null` when `instant` is null.

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatNumber.kt`

```kotlin
fun Double.formatDecimal(places: Int): String
```
_No KDoc at source._

```kotlin
fun Double.formatGrouped(places: Int): String
```
_No KDoc at source._

```kotlin
fun Long.formatGrouped(): String
```
_No KDoc at source._

---

_0 type(s), 5 function(s)/property(ies); 1 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
