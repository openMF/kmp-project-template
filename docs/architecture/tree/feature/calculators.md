# `feature/calculators/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 49 tracked files — 27× `.kt`, 20× `.xml`, 1× `.md`, 1× `.kts`

**27 Kotlin files** — 4 screen(s), 4 ViewModel(s).

Screens: `AffordabilityCalculatorScreen`, `AmortizationScreen`, `LoanCalcWizardScreen`, `LoanComparisonScreen`

ViewModels: `AffordabilityCalculatorViewModel`, `AmortizationViewModel`, `LoanCalcWizardViewModel`, `LoanComparisonViewModel`

**Consumes:** `core/store/banking`, `core/store/calc`, `core/store/config`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `OFFLINE_LOCAL_ONLY` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/calculators/README.md` | :feature:calculators |

<!-- tree-scaffold:end -->
## Significance

Four calculators — affordability, amortization, comparison and a multi-step wizard — and the widest
range of screen patterns in one module.

The wizard is the **`DraftSubmitHandler`** showcase: it persists its form on every step so a part-filled
wizard survives process death, and resumes with a Continue / Discard prompt. The other three are pure
local state with no Store at all, which is the other thing this module demonstrates — that the
framework does not force a Store where there is no data to fetch.
