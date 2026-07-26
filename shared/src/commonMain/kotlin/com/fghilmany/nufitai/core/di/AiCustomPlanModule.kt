package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.aicustomplan.datasource.AiCustomPlanRemoteDataSource
import com.fghilmany.nufitai.data.aicustomplan.repository.AiCustomPlanRepositoryImpl
import com.fghilmany.nufitai.domain.aicustomplan.repository.AiCustomPlanRepository
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanDraftHolder
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanFormViewModel
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanGateViewModel
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanPreviewViewModel
import com.fghilmany.nufitai.usecase.aicustomplan.ActivateAiCustomPlan
import com.fghilmany.nufitai.usecase.aicustomplan.BuildWorkoutHistorySummary
import com.fghilmany.nufitai.usecase.aicustomplan.CheckDraftQuota
import com.fghilmany.nufitai.usecase.aicustomplan.CheckInjuryConflicts
import com.fghilmany.nufitai.usecase.aicustomplan.DetectProfileConflicts
import com.fghilmany.nufitai.usecase.aicustomplan.GenerateAiPlan
import com.fghilmany.nufitai.usecase.aicustomplan.SaveAiCustomPlanDraft
import com.fghilmany.nufitai.usecase.aicustomplan.ValidateAndFixExerciseIds
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
import com.fghilmany.nufitai.usecase.planmanagement.DeletePlan
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. HttpClient now comes from `networkModule` (issue #83). */
val aiCustomPlanModule = module {
    single { AiCustomPlanRemoteDataSource(httpClient = get()) }
    single<AiCustomPlanRepository> { AiCustomPlanRepositoryImpl(remote = get()) }
    single { AiCustomPlanDraftHolder() }

    factory { BuildWorkoutHistorySummary(repository = get()) }
    factory { DetectProfileConflicts() }
    factory { GenerateAiPlan(repository = get()) }
    factory { ValidateAndFixExerciseIds(exerciseLibraryRepository = get()) }
    factory { CheckInjuryConflicts(exerciseLibraryRepository = get()) }
    factory { CheckDraftQuota(repository = get()) }
    factory { SaveAiCustomPlanDraft(repository = get()) }
    factory { ActivateAiCustomPlan(monthlyPlanRepository = get()) }

    viewModel { AiCustomPlanGateViewModel(getCurrentTier = get(), hasCompletedFullAssessment = get(), setDebugEntitlement = get()) }
    viewModel {
        AiCustomPlanFormViewModel(
            getLatestQuickAssessmentResult = get(),
            getLatestFullAssessmentResult = get(),
            buildWorkoutHistorySummary = get(),
            detectProfileConflicts = get(),
            generateAiPlan = get(),
            validateAndFixExerciseIds = get(),
            checkInjuryConflicts = get(),
            draftHolder = get(),
        )
    }
    viewModel {
        AiCustomPlanPreviewViewModel(
            checkDraftQuota = get(),
            saveAiCustomPlanDraft = get(),
            activateAiCustomPlan = get(),
            deletePlan = get(),
            getLatestFullAssessmentResult = get(),
            buildWorkoutHistorySummary = get(),
            generateAiPlan = get(),
            validateAndFixExerciseIds = get(),
            checkInjuryConflicts = get(),
            getExercisePool = get(),
            getExerciseAlternatives = get(),
            draftHolder = get(),
        )
    }
}
