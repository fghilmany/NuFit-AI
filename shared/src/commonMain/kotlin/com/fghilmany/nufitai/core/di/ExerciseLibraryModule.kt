package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.exerciselibrary.datasource.ExerciseLibraryLocalDataSource
import com.fghilmany.nufitai.data.exerciselibrary.datasource.ExerciseLibrarySeedDataSource
import com.fghilmany.nufitai.data.exerciselibrary.repository.ExerciseLibraryRepositoryImpl
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository
import com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel.ExerciseDetailViewModel
import com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel.ExerciseLibraryViewModel
import com.fghilmany.nufitai.usecase.exerciselibrary.FilterExercises
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseAlternatives
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseDetail
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001 -- mirrors `onboardingModule`'s shape exactly. */
val exerciseLibraryModule = module {
    single { ExerciseLibraryLocalDataSource(database = get()) }
    single { ExerciseLibrarySeedDataSource() }
    single<ExerciseLibraryRepository> { ExerciseLibraryRepositoryImpl(local = get(), seed = get()) }

    factory { GetExercisePool(repository = get()) }
    factory { FilterExercises() }
    factory { GetExerciseDetail(repository = get()) }
    factory { GetExerciseAlternatives(repository = get()) }

    viewModel { ExerciseLibraryViewModel(getExercisePool = get(), filterExercises = get()) }
    viewModel { ExerciseDetailViewModel(getExerciseDetail = get(), getExerciseAlternatives = get()) }
}
