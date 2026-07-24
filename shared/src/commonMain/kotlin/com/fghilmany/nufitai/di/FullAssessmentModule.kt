package com.fghilmany.nufitai.di

import com.fghilmany.nufitai.data.fullassessment.datasource.FullAssessmentLocalDataSource
import com.fghilmany.nufitai.data.fullassessment.repository.FullAssessmentRepositoryImpl
import com.fghilmany.nufitai.domain.fullassessment.repository.FullAssessmentRepository
import com.fghilmany.nufitai.presentation.fullassessment.viewmodel.FullAssessmentViewModel
import com.fghilmany.nufitai.usecase.fullassessment.CompleteFullAssessment
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.fullassessment.HasCompletedFullAssessment
import com.fghilmany.nufitai.usecase.fullassessment.SubmitFullAssessmentParQ
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. */
val fullAssessmentModule = module {
    single { FullAssessmentLocalDataSource(database = get()) }
    single<FullAssessmentRepository> { FullAssessmentRepositoryImpl(local = get()) }

    factory { SubmitFullAssessmentParQ() }
    factory { CompleteFullAssessment(repository = get()) }
    factory { GetLatestFullAssessmentResult(repository = get()) }
    factory { HasCompletedFullAssessment(repository = get()) }

    viewModel {
        FullAssessmentViewModel(
            submitFullAssessmentParQ = get(),
            completeFullAssessment = get(),
            activatePlanFromFullAssessment = get(), // registered in monthlyPlanModule -- both load together in KoinInit
        )
    }
}
