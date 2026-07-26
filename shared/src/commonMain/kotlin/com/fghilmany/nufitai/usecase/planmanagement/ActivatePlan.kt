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
        val activePlan = when (val result = repository.getActivePlan()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        if (activePlan != null && activePlan.id != planId) {
            when (val archived = repository.archivePlan(activePlan.id)) {
                is AppResult.Error -> return archived
                is AppResult.Success -> Unit
            }
        }
        return repository.activatePlan(planId)
    }
}
