# `tools/network-ksp/`

> Part of [`tools/`](../tools.md)  
> **Measured:** 3 tracked files — 1× `.kts`, 1× `.kt`, 1× `.SymbolProcessorProvider`

**Processes:** `@ApiBinding` (`kpt.core.base.network.annotation.ApiBinding`)

<!-- tree-scaffold:end -->
## Significance

Processes `@ApiBinding` and generates the Ktorfit bindings per access point. An API interface is declared
once and bound by its access-point id, so the base URL, the loggable host and the header provider all
come from `app-profile#network.access_points` rather than from the call site.

That is what makes an endpoint change a config edit rather than a code change.
