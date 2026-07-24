package com.fghilmany.nufitai.di

import com.fghilmany.nufitai.data.onboarding.datasource.OnboardingLocalDataSource
import com.fghilmany.nufitai.data.onboarding.repository.OnboardingRepositoryImpl
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.BodyDataViewModel
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.DoctorConsultViewModel
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.ParQViewModel
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QuickAssessmentViewModel
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.SplashViewModel
import com.fghilmany.nufitai.usecase.onboarding.AcknowledgeDoctorConsult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestParQResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLocalProfileStatus
import com.fghilmany.nufitai.usecase.onboarding.ResolveQuickAssessment
import com.fghilmany.nufitai.usecase.onboarding.SaveBodyMeasurement
import com.fghilmany.nufitai.usecase.onboarding.SubmitParQAnswers
import com.fghilmany.nufitai.usecase.onboarding.SubmitQuickAssessment
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Manual Koin module (not koin-annotations) -- koin-ksp-compiler's @ComponentScan-generated
 * module output proved non-deterministic across rebuilds/targets for this project's toolchain
 * (flipping between a named module and the deprecated defaultModule bucket unpredictably, even
 * with the Gradle build cache disabled). This is fully deterministic and still Koin, matching
 * tech-stack.md's locked "Koin, not kotlin-inject" decision -- just not the annotation flavor.
 */
val onboardingModule = module {
    single<OnboardingLocalDataSource> { OnboardingLocalDataSource(database = get()) }
    single<OnboardingRepository> { OnboardingRepositoryImpl(local = get()) }

    factory { ResolveQuickAssessment() }
    factory { SubmitParQAnswers(repository = get()) }
    factory { AcknowledgeDoctorConsult(repository = get()) }
    factory { GetLatestParQResult(repository = get()) }
    factory { GetLatestQuickAssessmentResult(repository = get()) }
    factory { SubmitQuickAssessment(repository = get(), resolveQuickAssessment = get()) }
    factory { SaveBodyMeasurement(repository = get()) }
    factory { GetLocalProfileStatus(repository = get()) }

    viewModel { SplashViewModel(getLocalProfileStatus = get()) }
    viewModel { ParQViewModel(submitParQAnswers = get()) }
    viewModel { DoctorConsultViewModel(getLatestParQResult = get(), acknowledgeDoctorConsult = get()) }
    viewModel {
        QuickAssessmentViewModel(
            submitQuickAssessment = get(),
            generateLocalTemplatePlan = get(), // registered in monthlyPlanModule -- both load together in KoinInit
        )
    }
    viewModel { BodyDataViewModel(saveBodyMeasurement = get()) }
}
