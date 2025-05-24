/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database.di

import kotlinx.coroutines.CoroutineDispatcher
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mifos.core.common.di.AppDispatchers
import org.mifos.core.database.AppDatabase
import template.core.base.database.AppDatabaseFactory
import kotlin.coroutines.CoroutineContext

actual val platformModule: Module = module {
    single {
        AppDatabaseFactory(
            androidApplication(),
        )
            .createDatabase(
                databaseClass = AppDatabase::class.java,
                databaseName = AppDatabase.DATABASE_NAME,
            )
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setQueryCoroutineContext(get<CoroutineDispatcher>(named(AppDispatchers.IO.name)) as CoroutineContext)
            .build()
    }
}
