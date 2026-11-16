package com.fghilmany.nufitai.usecase.assessmentdetail

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/**
 * UC-3 (03-assessment-detail.md, issue #77): reports which source produced the current active
 * plan, so the caller (P-09) knows whether to re-enter the Quick Assessment wizard
 * (`LOCAL_TEMPLATE`) or Full Assessment (`LOGGED_IN_RULE_ENGINE`) next.
 *
 * Read-only -- does NOT archive the active plan. The wizard/Full-Assessment flows
 * (`QuickAssessmentViewModel.submit()` / `ActivatePlanFromFullAssessment`) already
 * generate-then-`archiveAllActivePlansExcept(newPlan.id)` once the user actually resubmits.
 * Archiving here instead, before the user necessarily finishes the wizard, left the app with
 * zero active plans whenever they backed out mid-flow -- found in testing, fixed by moving the
 * archive to only happen alongside a real replacement.
 */
class RetakeAssessment(private val monthlyPlanRepository: MonthlyPlanRepository) {
    suspend operator fun invoke(): AppResult<PlanSource> = when (val result = monthlyPlanRepository.getActivePlan()) {
        is AppResult.Success -> result.data?.source?.let { AppResult.Success(it) }
            ?: AppResult.Error(Failure.Database("No active plan to retake"))
        is AppResult.Error -> result
    }
}
