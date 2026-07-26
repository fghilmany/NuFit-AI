package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/**
 * AC-7 -- mirrors `ActivatePlanFromFullAssessment`'s archive-and-activate pattern. [plan]/[days]
 * must not already be persisted: this pass's Preview screen only reaches Activate from an
 * in-memory, not-yet-saved draft -- activating an existing saved draft from a list is
 * `07-plan-management.md`'s scope, not built here (techspec §3 out-of-scope).
 */
class ActivateAiCustomPlan(private val monthlyPlanRepository: MonthlyPlanRepository) {
    suspend operator fun invoke(plan: MonthlyPlan, days: List<PlanDay>): AppResult<MonthlyPlan> {
        val activePlan = when (val result = monthlyPlanRepository.getActivePlan()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        if (activePlan != null) {
            when (val archived = monthlyPlanRepository.archivePlan(activePlan.id)) {
                is AppResult.Error -> return archived
                is AppResult.Success -> Unit
            }
        }

        val activatedPlan = plan.copy(status = PlanStatus.ACTIVE, source = PlanSource.AI_GENERATED)
        return when (val saved = monthlyPlanRepository.savePlan(activatedPlan, days)) {
            is AppResult.Success -> AppResult.Success(activatedPlan)
            is AppResult.Error -> saved
        }
    }
}
