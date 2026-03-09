package org.mifos.core.database.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.mifos.core.database.repository.SQLDelightSampleRepositoryImpl
import org.mifos.core.database.repository.SampleRepository


val TestSQLDelightModule = module {
    includes(testSQLDelightPlatformModule)
    single<SampleRepository> { SQLDelightSampleRepositoryImpl(get()) }
}

expect val testSQLDelightPlatformModule: Module