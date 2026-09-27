# `feature/home/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 36 tracked files — 21× `.xml`, 11× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**11 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `HomeScreen`

ViewModels: `HomeViewModel`

**Consumes:** `core/store/banking`, `core/store/currency`, `core/store/economic`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `MEMORY_ONLY`, `NETWORK_WITH_CACHE`, `OFFLINE_LOCAL_ONLY`, `PERIODIC` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/home/README.md` | :feature:home |

<!-- tree-scaffold:end -->
## Significance

The dashboard, and the **4-way `combineScreenStates` fan-in**: loans summary, upcoming bills, rates and
an exchange rate, each an independent card with its own loading, empty, error and content state.

It is also a **fork seam**. `HomeDashboard` is public rather than internal because `cmp-navigation`'s
`BackboneRegistry.homeBody` renders it as the default home body, and it lives in the fork-owned `demo/`
package so `--clean` removes it together with that default. A fork replaces the body without touching
the shell.
