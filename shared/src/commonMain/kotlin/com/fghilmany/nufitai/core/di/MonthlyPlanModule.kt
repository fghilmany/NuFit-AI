package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.monthlyplan.datasource.MonthlyPlanLocalDataSource
import com.fghilmany.nufitai.data.monthlyplan.repository.MonthlyPlanRepositoryImpl
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.HomeViewModel
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.SessionDetailViewModel
import com.fghilmany.nufitai.usecase.monthlyplan.ActivatePlanFromFullAssessment
import com.fghilmany.nufitai.usecase.monthlyplan.FreezeCalendarOnAbsence
import com.fghilmany.nufitai.usecase.monthlyplan.GenerateLocalTemplatePlan
import com.fghilmany.nufitai.usecase.monthlyplan.GenerateLoggedInPlan
import com.fghilmany.nufitai.usecase.monthlyplan.GenerateNextMonthPlan
import com.fghilmany.nufitai.usecase.monthlyplan.GetActivePlanOverview
import com.fghilmany.nufitai.usecase.monthlyplan.GetSessionStatus
import com.fghilmany.nufitai.usecase.monthlyplan.RebuildRemainingDays
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Manual Koin DSL, per ADR-001. `usecase/monthlyplan/rules` (BuildMovementPool,
 * SafetyFilter, GoalPrescription, CalibrateStartingLevel, MonthlyScheduler, ProgressionGating) are
 * stateless objects, not classes -- not Koin-registered, called directly like
 * `ResolveQuickAssessment`'s pure-function siblings.
 */
val monthlyPlanModule = module {
    single { MonthlyPlanLocalDataSource(database = get()) }
    single<MonthlyPlanRepository> { MonthlyPlanRepositoryImpl(local = get()) }

    factory { GenerateLocalTemplatePlan(getExercisePool = get(), repository = get()) }
    factory { GenerateLoggedInPlan(getExercisePool = get(), repository = get()) }
    factory { ActivatePlanFromFullAssessment(monthlyPlanRepository = get(), getLatestQuickAssessmentResult = get(), generateLoggedInPlan = get()) }
    factory { GetSessionStatus() }
    factory { GenerateNextMonthPlan(monthlyPlanRepository = get(), getLatestQuickAssessmentResult = get(), generateLocalTemplatePlan = get(), generateLoggedInPlan = get()) }
    factory { RebuildRemainingDays(getExercisePool = get(), repository = get()) }
    factory { FreezeCalendarOnAbsence(monthlyPlanRepository = get()) }
    factory { GetActivePlanOverview(repository = get()) }

    viewModel { HomeViewModel(getActivePlanOverview = get(), getSessionStatus = get()) }
    viewModel { SessionDetailViewModel(getActivePlanOverview = get(), getSessionStatus = get(), getExercisePool = get()) }
}
