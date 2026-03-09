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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.mifos.core.database.di.TestSQLDelightModule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@RunWith(AndroidJUnit4::class)
class SQLDelightRepositoryAndroidTest : SQLDelightRepositoryTest() {

    @BeforeTest
    override fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        koinApp = startKoin {
            androidContext(context)
            modules(TestSQLDelightModule)
        }
        repository = koinApp!!.koin.get()
    }

    @AfterTest
    override fun teardown() {
        super.teardown()
    }
}
