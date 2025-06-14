/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import org.mifos.core.database.dao.TaskDao
import org.mifos.core.database.entity.TaskEntity
import org.mifos.core.database.utils.ChargeTypeConverters

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

@Database(
    entities = [
        TaskEntity::class,
    ],
    version = AppDatabase.VERSION,
    exportSchema = true,
    autoMigrations = [],
)
@TypeConverters(ChargeTypeConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
actual abstract class AppDatabase : RoomDatabase() {
    actual abstract val taskDao: TaskDao

    companion object {
        const val VERSION = 1
        const val DATABASE_NAME = "mifos_database.db"
    }
}
