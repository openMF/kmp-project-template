/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.migrations

import androidx.room3.DeleteTable
import androidx.room3.migration.AutoMigrationSpec

/**
 * Auto-migration spec for the demo showcase's v8 → v10 hop (collapsed from the never-shipped
 * v8→9 + v9→10 path).
 *
 * Instructs Room to DROP the `samples` table that was present in v1–v8 and is no longer part of the
 * schema. Adding the `alerts` and `interest_rate_series` tables is fully auto-handled by Room; only
 * the DROP requires an explicit declaration.
 *
 * Relocated OUT of `AppDatabase` (E1/C7): it was a nested `AppDatabase.MigrationSpec8to10`, which
 * pinned demo content inside the template-owned `@Database` file and forced that file to be
 * 3-way merged forever. As a top-level class under `demo/` it is deleted wholesale by
 * `scripts/remove-demo.sh`, and the migration that references it is declared in
 * `app-profile/app.yaml#database.auto_migrations` (demo-fenced), so `AppDatabase.kt` can FULL-COPY.
 */
@DeleteTable(tableName = "samples")
class MigrationSpec8to10 : AutoMigrationSpec
