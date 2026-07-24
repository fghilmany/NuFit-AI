package com.fghilmany.nufitai.di

import com.fghilmany.nufitai.data.exerciselibrary.datasource.ExerciseLibraryLocalDataSource
import com.fghilmany.nufitai.data.exerciselibrary.datasource.ExerciseLibrarySeedDataSource
import com.fghilmany.nufitai.data.exerciselibrary.repository.ExerciseLibraryRepositoryImpl
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001 -- mirrors `onboardingModule`'s shape exactly. */
val exerciseLibraryModule = module {
    single { ExerciseLibraryLocalDataSource(database = get()) }
    single { ExerciseLibrarySeedDataSource() }
    single<ExerciseLibraryRepository> { ExerciseLibraryRepositoryImpl(local = get(), seed = get()) }

    factory { GetExercisePool(repository = get()) }
}
