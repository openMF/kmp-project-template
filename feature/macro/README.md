# :feature:macro

Country-level macro indicators (GDP / CPI / unemployment) from World Bank Open Data — the B8
Country Macro Snapshot, a multi-source-combine showcase with a country picker.

- **Screens:** `CountryMacroScreen`, `CountryPickerScreen`, `MacroIndicatorDetailScreen`.
- **ViewModels:** `CountryMacroViewModel` — holds **three independent**
  `ScreenDataStream`s (GDP, Inflation, Unemployment) as three independent cells on
  `MacroUiState`, so one failing indicator never blocks the others; `CountryPickerViewModel`
  — no params, reads `SupportedCountries`; `MacroIndicatorDetailViewModel` — full-history
  detail for one `(countryCode, indicatorKind)` pair.
- **Routes:** `MacroGraphRoute` → `CountryMacroRoute(countryCode = "US")` (start),
  `CountryPickerRoute`, `IndicatorDetailRoute(countryCode, indicatorKindName)`. Entry point:
  `NavController.navigateToMacroGraph()`.
- **DI:** `MacroModule`.

See `FEATURE_AUTHORING.md` for the MEMORY_ONLY / multi-source-combine archetype pattern.
