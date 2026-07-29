package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.language.LocaleController
import com.fghilmany.nufitai.data.language.datasource.LanguageLocalDataSource
import com.fghilmany.nufitai.data.language.repository.LanguageRepositoryImpl
import com.fghilmany.nufitai.domain.language.repository.LanguageRepository
import com.fghilmany.nufitai.usecase.language.GetLanguage
import com.fghilmany.nufitai.usecase.language.SetLanguage
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. */
val languageModule = module {
    single { LanguageLocalDataSource(settings = get()) }
    single<LanguageRepository> { LanguageRepositoryImpl(local = get()) }
    single { LocaleController() }

    factory { GetLanguage(repository = get()) }
    factory { SetLanguage(repository = get(), localeController = get()) }
}
