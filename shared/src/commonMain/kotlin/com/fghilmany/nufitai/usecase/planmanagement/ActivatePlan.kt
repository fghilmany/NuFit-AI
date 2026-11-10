package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/**
 * P-14 (AC-2) -- covers both "Aktifkan" (from DRAFT) and "Pulihkan" (from ARCHIVED): the button
 * label differs by the target's prior status, but the operation is identical. Archives the
 * current active plan first (if one exists and isn't already the target), then flips the
 * target's status to ACTIVE -- no plan regeneration, the target's `plan_day`/`planned_exercise`
 * rows already exist from when it was first saved.
 */
class ActivatePlan(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(planId: String): AppResult<Unit> {
        when (val activated = repository.activatePlan(planId)) {
            is AppResult.Error -> return activated
            is AppResult.Success -> Unit
        }
        // Bulk archive (not a single getActivePlan()+archive) -- self-heals if more than one plan
        // was ever left ACTIVE (plan-management.md's "exactly 1 active plan" invariant).
        return repository.archiveAllActivePlansExcept(planId)
    }
}
