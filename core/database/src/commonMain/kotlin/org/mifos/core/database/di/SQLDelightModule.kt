package org.mifos.core.database.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.MifosSQLDelightDatabase

const val DB_FILE_NAME = "mifos-sqldelight-database.db"

val sqlDelightModule: Module = module {
    includes(driverModule)
    single(createdAtStart = true) {
        MifosSQLDatabaseInitializer(
            get(),
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        ).apply { initialize() }
    }
    single<MifosSQLDelightDatabase> {
        MifosSQLDelightDatabase(get())
    }
}
expect val driverModule: Module
