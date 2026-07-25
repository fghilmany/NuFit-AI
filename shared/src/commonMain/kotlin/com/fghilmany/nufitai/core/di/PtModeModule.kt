package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.ptmode.datasource.PtModeLocalDataSource
import com.fghilmany.nufitai.data.ptmode.repository.PtModeRepositoryImpl
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.PtModeViewModel
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.WorkoutSummaryViewModel
import com.fghilmany.nufitai.usecase.ptmode.ConfirmSet
import com.fghilmany.nufitai.usecase.ptmode.EndSessionEarly
import com.fghilmany.nufitai.usecase.ptmode.GetLatestLoggedSet
import com.fghilmany.nufitai.usecase.ptmode.GetLatestSessionForDay
import com.fghilmany.nufitai.usecase.ptmode.GetOrCreateExerciseLog
import com.fghilmany.nufitai.usecase.ptmode.GetOrCreateWorkoutSession
import com.fghilmany.nufitai.usecase.ptmode.GetWorkoutSummary
import com.fghilmany.nufitai.usecase.ptmode.SaveWorkoutSummary
import com.fghilmany.nufitai.usecase.ptmode.SkipExercise
import com.fghilmany.nufitai.usecase.ptmode.SkipRestTimer
import com.fghilmany.nufitai.usecase.ptmode.SwapExercise
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Manual Koin DSL, per ADR-001 -- mirrors `monthlyPlanModule`'s shape. Cross-feature usecases
 * (GetExercisePool, GetExerciseAlternatives from exerciseLibraryModule; GetActivePlanOverview
 * from monthlyPlanModule) are resolved via `get()` the same way monthlyPlanModule already
 * pulls GetExercisePool across module boundaries.
 */
val ptModeModule = module {
    single { PtModeLocalDataSource(database = get()) }
    single<PtModeRepository> { PtModeRepositoryImpl(local = get()) }

    factory { GetOrCreateWorkoutSession(repository = get()) }
    factory { GetOrCreateExerciseLog(repository = get()) }
    factory { ConfirmSet(repository = get()) }
    factory { SkipRestTimer(repository = get()) }
    factory { SkipExercise(repository = get()) }
    factory { SwapExercise(repository = get()) }
    factory { EndSessionEarly(repository = get()) }
    factory { GetLatestLoggedSet(repository = get()) }
    factory { GetLatestSessionForDay(repository = get()) }
    factory { GetWorkoutSummary(repository = get()) }
    factory { SaveWorkoutSummary(monthlyPlanRepository = get()) }

    viewModel {
        PtModeViewModel(
            getOrCreateWorkoutSession = get(),
            getOrCreateExerciseLog = get(),
            confirmSetUseCase = get(),
            skipRestTimerUseCase = get(),
            skipExerciseUseCase = get(),
            swapExerciseUseCase = get(),
            endSessionEarlyUseCase = get(),
            getLatestLoggedSet = get(),
            getActivePlanOverview = get(),
            getExercisePool = get(),
            getExerciseAlternatives = get(),
        )
    }
    viewModel {
        WorkoutSummaryViewModel(
            getLatestSessionForDay = get(),
            getWorkoutSummary = get(),
            saveWorkoutSummary = get(),
            getActivePlanOverview = get(),
            getExercisePool = get(),
        )
    }
}
