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
        val activatedPlan = plan.copy(status = PlanStatus.ACTIVE, source = PlanSource.AI_GENERATED)
        when (val saved = monthlyPlanRepository.savePlan(activatedPlan, days)) {
            is AppResult.Error -> return saved
            is AppResult.Success -> Unit
        }
        // Bulk archive (not a single getActivePlan()+archive) -- self-heals if more than one plan
        // was ever left ACTIVE (plan-management.md's "exactly 1 active plan" invariant).
        return when (val archived = monthlyPlanRepository.archiveAllActivePlansExcept(activatedPlan.id)) {
            is AppResult.Error -> archived
            is AppResult.Success -> AppResult.Success(activatedPlan)
        }
    }
}
