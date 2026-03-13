/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database

import kotlinx.coroutines.test.runTest
import org.mifos.core.database.di.schemaInitJob
import kotlin.test.BeforeTest

class SQLDelightRepositoryWasmJsTest : SQLDelightRepositoryTest() {
    @BeforeTest
    fun awaitSchemaCreation() = runTest {
        schemaInitJob?.join()
    }
}
