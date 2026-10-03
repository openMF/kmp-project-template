# `tests/`

> **Kind:** Test fixtures  
> **Measured:** 9 tracked files — 3× `.pbxproj`, 2× `.ours`, 2× `.theirs`, 1× `.sh`, 1× `.md`

## Shape

| Path | Files |
|---|---:|
| `tests/fixtures/` | 8 |
| `tests/customization-surface/` | 1 |

<!-- tree-scaffold:end -->
## Significance

Cross-cutting test fixtures — the ones that do not belong to any single module's `commonTest`.

Two other fixture trees exist and are easy to confuse with this one:
`scripts/product-health/tests/` holds deliberately-malformed RED fixtures for the health checks, and
`scripts/docs/tests/` holds deliberately-UNDOCUMENTED Kotlin for the doc scanner. Both are excluded
from detekt, because a fixture that exists to be wrong cannot also satisfy the linter — documenting a
red fixture deletes the evidence that the check works.
