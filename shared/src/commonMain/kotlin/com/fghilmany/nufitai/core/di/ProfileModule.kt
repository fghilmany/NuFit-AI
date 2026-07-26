package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.profile.datasource.ProfileLocalDataSource
import com.fghilmany.nufitai.data.profile.repository.ProfileRepositoryImpl
import com.fghilmany.nufitai.domain.profile.repository.ProfileRepository
import com.fghilmany.nufitai.usecase.profile.GetProfileBasics
import com.fghilmany.nufitai.usecase.profile.SaveProfileBasics
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. */
val profileModule = module {
    single { ProfileLocalDataSource(database = get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(local = get(), authRepository = get()) }

    factory { GetProfileBasics(repository = get()) }
    factory { SaveProfileBasics(repository = get()) }
}
