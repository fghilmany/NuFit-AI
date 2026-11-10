package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult

/**
 * AC-6: Full Assessment completion (not login alone) archives the active Local plan and
 * activates a new Logged-In plan -- mirrors `03-assessment-detail.md` UC-3's retake-archive
 * pattern. Log history is untouched by archiving (only `MonthlyPlan.status` changes).
 */
class ActivatePlanFromFullAssessment(
    private val monthlyPlanRepository: MonthlyPlanRepository,
    private val getLatestQuickAssessmentResult: GetLatestQuickAssessmentResult,
    private val generateLoggedInPlan: GenerateLoggedInPlan,
) {
    suspend operator fun invoke(fullAssessment: FullAssessmentResult): AppResult<MonthlyPlan> {
        val level = when (val result = getLatestQuickAssessmentResult()) {
            is AppResult.Success -> result.data?.level ?: Level.BEGINNER
            is AppResult.Error -> return result
        }

        val generated = when (val result = generateLoggedInPlan(fullAssessment, level)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }

        // Bulk archive (not a single getActivePlan()+archive) -- self-heals if more than one plan
        // was ever left ACTIVE (plan-management.md's "exactly 1 active plan" invariant).
        return when (val archived = monthlyPlanRepository.archiveAllActivePlansExcept(generated.id)) {
            is AppResult.Error -> archived
            is AppResult.Success -> AppResult.Success(generated)
        }
    }
}
