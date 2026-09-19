# `core/data`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATA.md`
> **Measured:** 62 Kotlin files, 21 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@RepositoryBinding` | `data-ksp` | di/GeneratedRepositoryBindings |
| `@DataProvider` | `data-ksp` | di/GeneratedRepositoryBindings, config/AppOutboxQualifiers |
| `@FromStore` | `data-ksp` |  |
| `@FromQualifier` | `data-ksp` |  |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AlertsRepository`, `AlertsRepositoryImpl`, `AmortizationCalcRepository`, `AmortizationCalcRepositoryImpl`, `BillReminderRepository`, `BillReminderRepositoryImpl`, `BillReminderSubmitSyncer`, `CloudTodoRepository`, `CloudTodoRepositoryImpl`, `CryptoRepository`, `CryptoRepositoryImpl`, `CurrencyRepository`, `CurrencyRepositoryImpl`, `EconomicRatesRepository`  …and 20 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
