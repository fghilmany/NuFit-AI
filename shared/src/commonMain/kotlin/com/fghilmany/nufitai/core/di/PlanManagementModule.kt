package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.MyPlansViewModel
import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.TemplatePickerViewModel
import com.fghilmany.nufitai.usecase.planmanagement.ActivatePlan
import com.fghilmany.nufitai.usecase.planmanagement.CreateTemplatePlan
import com.fghilmany.nufitai.usecase.planmanagement.DeletePlan
import com.fghilmany.nufitai.usecase.planmanagement.GetAllPlansForManagement
import com.fghilmany.nufitai.usecase.planmanagement.GetPlanProgress
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Manual Koin DSL, per ADR-001. */
val planManagementModule = module {
    factory { GetAllPlansForManagement(repository = get()) }
    factory { ActivatePlan(repository = get()) }
    factory { DeletePlan(repository = get()) }
    factory { CreateTemplatePlan(monthlyPlanRepository = get(), generateLocalTemplatePlan = get()) }
    factory { GetPlanProgress(repository = get()) }

    viewModel {
        MyPlansViewModel(
            getAllPlansForManagement = get(),
            activatePlan = get(),
            deletePlan = get(),
            checkDraftQuota = get(),
            getPlanProgress = get(),
        )
    }
    viewModel { TemplatePickerViewModel(createTemplatePlan = get()) }
}
