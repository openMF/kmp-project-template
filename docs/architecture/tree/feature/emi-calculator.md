# `feature/emi-calculator/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 31 tracked files — 20× `.xml`, 9× `.kt`, 1× `.md`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `EmiCalculatorScreen`

ViewModels: `EmiCalculatorViewModel`

**Consumes:** `core/store/emi`

### Docs in the tree

| Path | |
|---|---|
| `feature/emi-calculator/README.md` | :feature:emi-calculator |

<!-- tree-scaffold:end -->
## Significance

The single-purpose EMI calculator, and the **`MEMORY_ONLY`** showcase.

Store-backed even though nothing persists: the calculation is deterministic in its inputs, so the params
double as the cache key and dragging a tenure slider back over a value already tried is served from
memory. It also means the result reaches the screen as a `ScreenState` like every other read surface,
so the calculator renders through the same wrapper as the rest of the app rather than a bespoke
nullable `StateFlow`.
