package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel.AssessmentDetailViewModel
import com.fghilmany.nufitai.usecase.assessmentdetail.GetAssessmentDetail
import com.fghilmany.nufitai.usecase.assessmentdetail.GetAssessmentHistory
import com.fghilmany.nufitai.usecase.assessmentdetail.RetakeAssessment
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. Pure composition module -- no own datasource/repository (issue #77). */
val assessmentDetailModule = module {
    factory {
        GetAssessmentDetail(
            getLatestQuickAssessmentResult = get(),
            getLatestParQResult = get(),
            getLatestFullAssessmentResult = get(),
            getLatestBodyMeasurement = get(),
            monthlyPlanRepository = get(),
        )
    }
    factory { GetAssessmentHistory(monthlyPlanRepository = get()) }
    factory { RetakeAssessment(monthlyPlanRepository = get()) }

    viewModel {
        AssessmentDetailViewModel(
            getAssessmentDetail = get(),
            getAssessmentHistory = get(),
            retakeAssessment = get(),
        )
    }
}
