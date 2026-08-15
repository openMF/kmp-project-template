# :feature:alerts

Price alerts — list plus a create form, the toolkit's canonical `submit_offline_write` demo.

- **Screens:** `AlertsListScreen`, `AlertCreateScreen`.
- **ViewModels:** `AlertsListViewModel` — pure passthrough of
  `AlertsRepository.alertsStream` (OFFLINE_LOCAL_ONLY read side); `AlertCreateViewModel` —
  extends `BaseMutationViewModel` in `MutationMode.Draft`, persisting the draft to the
  `framework_submit_drafts` outbox (`SubmitOutbox<PriceAlert>`, DI-qualified
  `OutboxQualifiers.PriceAlert`) before it hits the repository, so an offline submit survives
  and retries via `OfflineSubmitSyncer`.
- **Routes:** `AlertsGraphRoute` (root) → `AlertsListRoute` (start) → `AlertCreateRoute`.
  Entry point: `NavController.navigateToAlertsGraph()`.
- **DI:** `AlertsModule`.

See `FEATURE_AUTHORING.md` for the archetype → module chain and the MUTABLE/Draft write path.
