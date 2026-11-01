# `tests/customization-surface/`

> Part of [`tests/`](../tests.md)  
> **Measured:** 3 tracked files — 3× `.sh`

3 tracked files.

<!-- tree-scaffold:end -->
## Significance

Functional tests for the template-sync ownership engine. Each runs the REAL functions — extracted from
`scripts/customization-surface.sh` and `scripts/white-label/sync-dirs.sh` — against a throwaway git
repository, so a passing test proves the shipped code, not a copy of it.

| Test | Proves |
|---|---|
| `merge-3way-test.sh` | `cs_merge_3way` keeps a fork's added `<uses-permission>` when the template updates the manifest |
| `mirror-test.sh` | `--mirror` removes template-owned files the fork added, leaves fork-owned and held files alone, and never touches a file the template ships |
| `sync-roots-test.sh` | a path outside every sync root resolves `fork`, so a fork's own top-level directory is never claimed by the template |

`mirror-test.sh` needs bash 4: the engine's `EXCLUSIONS` map is an associative array.
