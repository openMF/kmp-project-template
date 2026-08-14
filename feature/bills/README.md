# :feature:bills

Recurring bill reminders — dashboard plus an add/edit form, the `DraftSubmitHandler`
(offline-resilient form) showcase, with in-app notification scheduling.

- **Screens:** `BillRemindersListScreen`, `AddOrEditBillReminderScreen`.
- **ViewModels:** `BillRemindersListViewModel` — combines `observeAll()`,
  `observeUpcoming(7)`, and `observeTotalUpcomingAmount(30)` into one dashboard state;
  `EditBillReminderViewModel` — a `BaseMutationViewModel` (`MutationMode.Draft`) showcase,
  draft-persisted via `SubmitOutbox<BillReminder>` (`OutboxQualifiers.BillReminder`).
- **Routes:** `BillsGraphRoute` (root) → `BillRemindersListRoute` (start),
  `AddOrEditBillReminderRoute(billId?)` (`null` = create, non-null = edit). Entry point:
  `NavController.navigateToBills()`.
- Reminders schedule through `BillNotificationGateway`, a feature-local seam onto the
  cross-platform `sync` module's `WorkScheduler` (worker-kmp + KMPNotifier) — no
  per-platform scheduler code lives here.
- **DI:** `BillsModule`.

See `FEATURE_AUTHORING.md` for the MUTABLE/Draft write path this feature demonstrates.
