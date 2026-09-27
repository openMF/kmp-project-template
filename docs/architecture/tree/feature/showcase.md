# `feature/showcase/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 2 screen(s), 0 ViewModel(s).

Screens: `StateGalleryScreen`, `TransitionGalleryScreen`

### Docs in the tree

| Path | |
|---|---|
| `feature/showcase/README.md` | :feature:showcase |

<!-- tree-scaffold:end -->
## Significance

The developer-facing galleries: every `ScreenState` rendered side by side, and every motion transition
demonstrable on a device.

Their value is regression-catching. A state gallery makes a broken empty state visible in one screen
instead of requiring a specific data condition, and the transition gallery is how a motion change is
reviewed before it ships across every navigation edge. `--clean` removes them with the rest of the demo.
