/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package template.core.base.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

class AppDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
    ): RoomDatabase.Builder<T> {
        val dbPath = getDatabasePath(databaseName)
        dbPath.parentFile?.mkdirs()
        return Room.databaseBuilder<T>(
            name = dbPath.absolutePath,
        )
    }

    @PublishedApi
    internal fun getDatabasePath(databaseName: String): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        val appDataDir = when {
            os.contains("win") -> File(System.getenv("APPDATA"), "MifosDatabase")
            os.contains("mac") -> File(userHome, "Library/Application Support/MifosDatabase")
            else -> File(userHome, ".local/share/MifosDatabase")
        }
        return File(appDataDir, databaseName)
    }
}
