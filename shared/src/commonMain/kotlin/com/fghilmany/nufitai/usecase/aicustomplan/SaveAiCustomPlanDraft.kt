package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/** UC-4 alternate flow -- "Simpan Draft" without activating. Caller checks [CheckDraftQuota] first. */
class SaveAiCustomPlanDraft(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(plan: MonthlyPlan, days: List<PlanDay>): AppResult<MonthlyPlan> {
        val draftPlan = plan.copy(status = PlanStatus.DRAFT, source = PlanSource.AI_GENERATED)
        return when (val saved = repository.savePlan(draftPlan, days)) {
            is AppResult.Success -> AppResult.Success(draftPlan)
            is AppResult.Error -> saved
        }
    }
}
