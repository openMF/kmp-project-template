/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database

/**
 * Fork-owned database schema offset — the ONE line a fork edits when it changes its own schema.
 *
 * White-label seam (owner: fork, PRESERVED across `/kmp-project-template-sync`). A fork:
 *  1. adds its `@Entity` classes inside the `// fork:begin`/`// fork:end` block of [AppDatabase]'s
 *     `entities = [...]`,
 *  2. adds its `abstract val fooDao: FooDao` in the fork block of the DAO accessors,
 *  3. adds a matching `AutoMigration(from = N, to = N+1)` in the fork block of `autoMigrations`,
 *  4. bumps [VERSION_OFFSET] by 1 here.
 *
 * The effective DB version is `AppDatabase.TEMPLATE_BASE_VERSION + VERSION_OFFSET`, so a fork's
 * version bump NEVER edits the same line the template's base version lives on — template schema
 * updates and fork schema additions live in disjoint regions/files and merge without conflict.
 *
 * A fresh fork keeps this at 0. Increment once per fork schema-changing migration.
 */
object ForkDatabaseConfig {
    const val VERSION_OFFSET = 0
}
