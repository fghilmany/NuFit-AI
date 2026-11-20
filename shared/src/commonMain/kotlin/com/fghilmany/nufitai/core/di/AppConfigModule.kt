package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.appconfig.datasource.AppConfigRemoteDataSource
import com.fghilmany.nufitai.data.appconfig.repository.AppConfigRepositoryImpl
import com.fghilmany.nufitai.domain.appconfig.repository.AppConfigRepository
import com.fghilmany.nufitai.usecase.appconfig.CheckForceUpdate
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. */
val appConfigModule = module {
    single { AppConfigRemoteDataSource(httpClient = get()) }
    single<AppConfigRepository> { AppConfigRepositoryImpl(remote = get()) }

    factory { CheckForceUpdate(repository = get()) }
}
