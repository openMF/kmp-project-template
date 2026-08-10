/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.shared

/*
 * ForkWorkerDeclarations — the FORK-OWNED white-label seam for a fork's own background workers.
 *
 * The worker-kmp KSP processor scans cmp-shared/commonMain for EVERY `@WorkerKmpWorkers` site and
 * aggregates them with the template's base workers (see WorkerDeclarations.kt). So declare YOUR fork's
 * workers HERE — never edit the template WorkerDeclarations.kt. This file is `owner: fork` in
 * customization-surface.yaml, so a `sync-dirs` / `white-label-doctor` template sync NEVER overwrites it
 * while it full-copies the template infra.
 *
 * To add a worker, uncomment + fill (each worker class must be a `CoroutineWorker` per worker-kmp):
 *
 *   import io.github.mobilebytelabs.worker.app.WorkerKmpWorkers
 *
 *   @WorkerKmpWorkers(
 *       workers = [
 *           MyFeatureSyncWorker::class,
 *       ],
 *   )
 *   public fun forkWorkerDeclarations(): Unit = Unit
 *
 * Leave this file annotation-free when the fork has no extra workers (the KSP processor simply finds
 * nothing to add here and uses the template's base worker set).
 */
