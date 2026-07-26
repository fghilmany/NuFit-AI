package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/**
 * P-14 (AC-4/AC-5) -- source-agnostic (template, rule-engine, or AI, unlike issue #81's
 * AI-only `DeleteAiCustomPlan`, which this supersedes). Rejects the active plan regardless of
 * source; otherwise soft-deletes (never a physical DELETE -- see `MonthlyPlanRepository.softDeletePlan`).
 */
class DeletePlan(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(planId: String): AppResult<Unit> {
        val plan = when (val result = repository.getAllPlans()) {
            is AppResult.Success -> result.data.find { it.id == planId } ?: return AppResult.Success(Unit)
            is AppResult.Error -> return result
        }
        if (plan.status == PlanStatus.ACTIVE) {
            return AppResult.Error(Failure.Validation("Cannot delete the active plan -- switch plans first"))
        }
        return repository.softDeletePlan(planId)
    }
}
