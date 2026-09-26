# `core/ui`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_UI.md`
> **Measured:** 14 Kotlin files, 0 test files

**Defines annotations:** `@FeatureTab`

## Principal types

`FeatureTab`, `FloatingActionButtonContent`, `KptPullToRefreshState`, `NavigationItem`, `PasswordChecker`, `PasswordStrength`, `PasswordStrengthResult`, `PasswordStrengthState`, `RevealDirection`, `RevealState`, `RevealValue`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/ui sha=ae5a8014bd3826e5e0e5057b22c182744ef1047c -->
## API reference

_Generated from `core/ui` at tree `ae5a8014bd38` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt`

```kotlin
fun KptBottomBar(
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationBarItem.kt`

```kotlin
fun RowScope.KptNavigationBarItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:36</code></summary>

```kotlin
    ) {
        navigationItems.forEach { navigationItem ->
            KptNavigationBarItem(
                contentDescriptionRes = navigationItem.contentDescriptionRes,
                selectedIcon = navigationItem.selectedIcon,
                unselectedIcon = navigationItem.icon,
                isSelected = selectedItem == navigationItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt`

```kotlin
fun KptNavigationRail(
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRailItem.kt`

```kotlin
fun ColumnScope.KptNavigationRailItem(
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt:60</code></summary>

```kotlin
        ) {
            navigationItems.forEach { navigationItem ->
                KptNavigationRailItem(
                    contentDescriptionRes = navigationItem.contentDescriptionRes,
                    selectedIconRes = navigationItem.selectedIcon,
                    unselectedIconRes = navigationItem.icon,
                    isSelected = navigationItem == selectedItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/PasswordStrengthIndicator.kt`

```kotlin
fun PasswordStrengthIndicator(
```
_No KDoc at source._

```kotlin
enum class PasswordStrengthState
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/RevealSwipe.kt`

```kotlin
fun RevealSwipe(
```
_No KDoc at source._

```kotlin
fun BaseRevealSwipe(
```
_No KDoc at source._

```kotlin
enum class RevealDirection
```
_No KDoc at source._

```kotlin
enum class RevealValue
```
Possible values of `RevealState`.

```kotlin
fun rememberRevealState(
```
Create and `remember` a `RevealState` with the default animation clock.

```kotlin
data class RevealState(
```
_No KDoc at source._

```kotlin
suspend fun RevealState.reset()
```
Reset the component to the default position, with an animation.

```kotlin
suspend fun RevealState.resetFast()
```
Reset the component to the default position, with an animation.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/FeatureTab.kt`

```kotlin
public annotation class FeatureTab
```
Marks a `NavigationItem` object as a bottom-navigation tab contributed by a feature.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/NavigationItem.kt`

```kotlin
interface NavigationItem
```
Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:24</code></summary>

```kotlin
@Composable
fun KptBottomBar(
    navigationItems: List<NavigationItem>,
    selectedItem: NavigationItem?,
    onClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
```

</details>

- `val selectedIcon: ImageVector` — The resource ID for the icon representing the tab when it is selected.
- `val icon: ImageVector` — Resource id for the icon representing the tab.
- `val labelRes: StringResource` — Resource id for the label describing the tab.
- `val contentDescriptionRes: StringResource` — Resource id for the content description describing the tab.
- `val graphRoute: String` — Route of the tab's graph.
- `val startDestinationRoute: String` — Route of the tab's start destination.
- `val testTag: String` — The test tag of the tab.
- `val inlineTab: Boolean get() = true` — Whether this tab renders INLINE inside the navbar scaffold — its content swaps within the inner NavHost so the bottom bar stays visible and the tab keeps its own back stack (exactly like the backbone Home/Profile tabs) — versus a FULL-SCREEN destination pushed on the outer authenticated graph (the bottom bar is hidden; e.g. an immersive focus-timer that declares `bottom_navigation_visible: false`). Default `true` (inline). A full-screen tab overrides to `false`. An inline extra tab (beyond Home/Profile) MUST also register its start destination via `TabRegistry.extraInlineTabDestinations` so the inner NavHost can host it; otherwise the tab has nothing to render inline.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptPullToRefreshState.kt`

```kotlin
data class KptPullToRefreshState(
```
Data class representing the pull-to-refresh state and behavior.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:50</code></summary>

```kotlin
    containerColor: Color = KptTheme.colorScheme.background,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
```

</details>

```kotlin
fun rememberKptPullToRefreshState(
```
Remembers and returns a `KptPullToRefreshState` instance.

```kotlin
fun rememberKptPullToRefreshState(
```
Bridge: derive a `KptPullToRefreshState` from a `PagingScreenStream` so `KptScaffold` / `KptRootScaffold` can drive pull-to-refresh on paginated screens without the consumer wiring isRefreshing/onRefresh manually.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(viewModel.pagingStream),
    ...
) { ... }
// and disable PagingScreenContent's built-in pull-to-refresh:
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::onRetry,
    enablePullToRefresh = false,   // scaffold owns the gesture
) { items(coins) { ... } }
```

</details>

```kotlin
fun <T> rememberKptPullToRefreshState(
```
Bridge for non-paginated single-key screens driven by `ScreenDataStream`. Use with `KptScaffold` for detail pages that benefit from pull-to-refresh.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(
        stream = viewModel.stream,
        currentState = uiState,  // your collectAsStateWithLifecycle value
    ),
    ...
) { ... }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt`

```kotlin
fun KptScaffold(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:135</code></summary>

```kotlin
    }

    KptScaffold(
        title = stringResource(Res.string.feature_settings_sync_drafts_title),
        onNavigationIconClick = onBackClick,
        modifier = modifier,
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:135</code></summary>

```kotlin
    }

    KptScaffold(
        title = stringResource(Res.string.feature_settings_sync_drafts_title),
        onNavigationIconClick = onBackClick,
        modifier = modifier,
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:135</code></summary>

```kotlin
    }

    KptScaffold(
        title = stringResource(Res.string.feature_settings_sync_drafts_title),
        onNavigationIconClick = onBackClick,
        modifier = modifier,
    ) {
```

</details>

```kotlin
data class FloatingActionButtonContent(
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordChecker.kt`

```kotlin
object PasswordChecker
```
_No KDoc at source._

- `fun getPasswordStrengthResult(password: String): PasswordStrengthResult`
- `val result = getPasswordStrength(password)`
- `fun getPasswordStrength(password: String): PasswordStrength`
- `val length = password.length`
- `val hasUpperCase = password.any { it.isUpperCase() }`
- `val hasLowerCase = password.any { it.isLowerCase() }`
- `val hasNumbers = password.any { it.isDigit() }`
- `val hasSymbols = password.any { !it.isLetterOrDigit() }`
- `val numTypesPresent =`
- `val entropyBits = calculateEntropy(password)`
- `val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols`
- `fun getPasswordFeedback(password: String): List<String>`
- `val feedback = mutableListOf<String>()`

```kotlin
sealed class PasswordStrengthResult
```
_No KDoc at source._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrength.kt`

```kotlin
enum class PasswordStrength
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrengthExtensions.kt:17</code></summary>

```kotlin
 */
@Suppress("MagicNumber")
fun Int.toPasswordStrengthOrNull(): PasswordStrength? = when (this) {
    0 -> PasswordStrength.LEVEL_0
    1 -> PasswordStrength.LEVEL_1
    2 -> PasswordStrength.LEVEL_2
    3 -> PasswordStrength.LEVEL_3
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrengthExtensions.kt`

```kotlin
fun Int.toPasswordStrengthOrNull(): PasswordStrength? = when (this)
```
Converts the given `Int` to a `PasswordStrength`. A `null` value is returned if this value is not in the [0, 4] range.

```kotlin
fun PasswordStrength.toInt(): Int = when (this)
```
Converts the given `PasswordStrength` to an `Int`.

---

_11 type(s), 39 function(s)/property(ies); 20 carry KDoc at source; 2 authored example(s); 8 live call site(s)._
<!-- api-docs:end -->
