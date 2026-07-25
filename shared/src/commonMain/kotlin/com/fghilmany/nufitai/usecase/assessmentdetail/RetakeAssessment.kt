package com.fghilmany.nufitai.usecase.assessmentdetail

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/**
 * UC-3 (03-assessment-detail.md, issue #77): archives the active plan and reports which
 * source produced it, so the caller (P-09) knows whether to re-enter the Quick Assessment
 * wizard (`LOCAL_TEMPLATE`) or Full Assessment (`LOGGED_IN_RULE_ENGINE`) next. Mirrors
 * `usecase/monthlyplan/ActivatePlanFromFullAssessment`'s get-active-then-archive shape.
 *
 * Regeneration itself is NOT this usecase's job -- the wizard/Full-Assessment flows already
 * generate+activate a fresh plan once the user resubmits, and by the time they do, no plan
 * is active (archived here), so they proceed exactly as they would on first-time onboarding.
 */
class RetakeAssessment(private val monthlyPlanRepository: MonthlyPlanRepository) {
    suspend operator fun invoke(): AppResult<PlanSource> {
        val activePlan = when (val result = monthlyPlanRepository.getActivePlan()) {
            is AppResult.Success -> result.data
                ?: return AppResult.Error(Failure.Database("No active plan to retake"))
            is AppResult.Error -> return result
        }

        return when (val archived = monthlyPlanRepository.archivePlan(activePlan.id)) {
            is AppResult.Error -> archived
            is AppResult.Success -> AppResult.Success(activePlan.source)
        }
    }
}
