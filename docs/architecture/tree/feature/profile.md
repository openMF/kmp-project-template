# `feature/profile/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 35 tracked files — 21× `.xml`, 10× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**10 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `ProfileScreen`

ViewModels: `ProfileViewModel`

**Consumes:** `core/store/profile`

### Docs in the tree

| Path | |
|---|---|
| `feature/profile/README.md` | :feature:profile |

<!-- tree-scaffold:end -->
## Significance

A signed-out placeholder, and a deliberate demonstration of where the seam is. `ProfileInfo` carries
only the app's display name, yet it is a real domain model rather than a raw `String` — because a fork
replaces the *fetcher*, not the screen. Swapping in a signed-in user means widening the model and
returning it from the store, with the ViewModel and composable untouched.
