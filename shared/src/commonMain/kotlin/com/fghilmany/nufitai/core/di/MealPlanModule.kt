package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.notification.MealReminderScheduler
import com.fghilmany.nufitai.data.mealplan.datasource.MealPlanLocalDataSource
import com.fghilmany.nufitai.data.mealplan.datasource.MealPlanRemoteDataSource
import com.fghilmany.nufitai.data.mealplan.datasource.MealPreferenceLocalDataSource
import com.fghilmany.nufitai.data.mealplan.datasource.MealReminderPreferenceLocalDataSource
import com.fghilmany.nufitai.data.mealplan.repository.MealPlanRepositoryImpl
import com.fghilmany.nufitai.data.mealplan.repository.MealPreferenceRepositoryImpl
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository
import com.fghilmany.nufitai.domain.mealplan.repository.MealPreferenceRepository
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealDetailViewModel
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPlanGateViewModel
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPlanViewModel
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPreferenceViewModel
import com.fghilmany.nufitai.usecase.mealplan.CalculateTdeeAndMacros
import com.fghilmany.nufitai.usecase.mealplan.GenerateMealPlan
import com.fghilmany.nufitai.usecase.mealplan.GetMealLogForPlannedMeal
import com.fghilmany.nufitai.usecase.mealplan.GetMealLogsForPlan
import com.fghilmany.nufitai.usecase.mealplan.GetMealPlanForWeek
import com.fghilmany.nufitai.usecase.mealplan.GetMealPreference
import com.fghilmany.nufitai.usecase.mealplan.GetMealReminderState
import com.fghilmany.nufitai.usecase.mealplan.GetPlannedMealById
import com.fghilmany.nufitai.usecase.mealplan.GetPlannedMealsForPlan
import com.fghilmany.nufitai.usecase.mealplan.LogMealStatus
import com.fghilmany.nufitai.usecase.mealplan.SaveMealPreference
import com.fghilmany.nufitai.usecase.mealplan.SetMealReminderEnabled
import com.fghilmany.nufitai.usecase.mealplan.ShouldSuggestRegenerate
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. techspec meal-plan.md §7. */
val mealPlanModule = module {
    single { MealPlanLocalDataSource(database = get()) }
    single { MealPreferenceLocalDataSource(database = get()) }
    single { MealPlanRemoteDataSource(httpClient = get()) }
    single<MealPlanRepository> { MealPlanRepositoryImpl(local = get(), remote = get(), authRepository = get()) }
    single<MealPreferenceRepository> { MealPreferenceRepositoryImpl(local = get()) }

    single { MealReminderPreferenceLocalDataSource(settings = get()) }
    single { MealReminderScheduler() }

    // GetLatestBodyMeasurement (onboardingModule), GetLatestFullAssessmentResult
    // (fullAssessmentModule), GetCurrentTier (entitlementModule) -- all load together in KoinInit.
    factory { CalculateTdeeAndMacros() }
    factory { GenerateMealPlan(repository = get()) }
    factory { GetMealPlanForWeek(repository = get()) }
    factory { GetPlannedMealsForPlan(repository = get()) }
    factory { GetMealLogsForPlan(repository = get()) }
    factory { GetPlannedMealById(repository = get()) }
    factory { GetMealLogForPlannedMeal(repository = get()) }
    factory { LogMealStatus(repository = get()) }
    factory { ShouldSuggestRegenerate(getLatestBodyMeasurement = get()) }
    factory { GetMealPreference(repository = get()) }
    factory { SaveMealPreference(repository = get()) }
    factory { GetMealReminderState(preference = get(), scheduler = get()) }
    factory { SetMealReminderEnabled(preference = get(), scheduler = get()) }

    viewModel {
        MealPlanGateViewModel(
            getCurrentTier = get(),
            getLatestBodyMeasurement = get(),
            getLatestFullAssessmentResult = get(),
            setDebugEntitlement = get(),
        )
    }
    viewModel {
        MealPlanViewModel(
            getMealPlanForWeek = get(),
            getPlannedMealsForPlan = get(),
            getMealLogsForPlan = get(),
            generateMealPlan = get(),
            calculateTdeeAndMacros = get(),
            getLatestBodyMeasurement = get(),
            getLatestFullAssessmentResult = get(),
            getMealPreference = get(),
            shouldSuggestRegenerate = get(),
            getMealReminderState = get(),
            setMealReminderEnabled = get(),
        )
    }
    viewModel { MealDetailViewModel(getPlannedMealById = get(), getMealLogForPlannedMeal = get(), logMealStatus = get()) }
    viewModel { MealPreferenceViewModel(getMealPreference = get(), saveMealPreference = get()) }
}
