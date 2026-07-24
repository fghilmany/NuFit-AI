package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult

/**
 * AC-4: triggers on day 30/31, archives the completed cycle, generates the next 30-day cycle.
 *
 * KNOWN GAP -- AC-5 ("kehadiran rendah -> intensitas tidak dinaikkan") is only PARTIALLY
 * satisfied: neither generator currently escalates `startingLevelPerPola` cycle-over-cycle in
 * the first place (Logged-In tier's `KalibrasiStartingLevel` always falls through to its safe
 * CAL-05 default since capacity-test norm tables aren't wired -- see that class's doc -- and
 * Local tier's level is fixed at Quick Assessment time). So low attendance can't accidentally
 * raise intensity today, but there is also no explicit gate actively enforcing AC-5 against a
 * future escalation mechanism (e.g. carrying `GatingProgresi`'s per-checkpoint level-ups
 * forward into next cycle's starting level). Needs revisiting once that carry-forward exists.
 *
 * Per issue #18's developer note ("siklus berikutnya dibangun ulang dari Tahap 6 & 7 yang
 * dihitung ulang, bukan melanjutkan level lama secara mentah"): this regenerates from the same
 * inputs (Quick Assessment answer / Full Assessment result) rather than blindly carrying the
 * prior cycle's `startingLevelPerPola` forward -- a full re-assessment-driven regeneration
 * (Tahap 9) is a separate, not-yet-built flow; this is the automatic same-cycle-2/3 rollover.
 */
class GenerateNextMonthPlan(
    private val monthlyPlanRepository: MonthlyPlanRepository,
    private val getLatestQuickAssessmentResult: GetLatestQuickAssessmentResult,
    private val generateLocalTemplatePlan: GenerateLocalTemplatePlan,
    private val generateLoggedInPlan: GenerateLoggedInPlan,
) {
    suspend operator fun invoke(fullAssessment: FullAssessmentResult?): AppResult<MonthlyPlan> {
        val activePlan = when (val result = monthlyPlanRepository.getActivePlan()) {
            is AppResult.Success ->
                result.data ?: return AppResult.Error(Failure.Database("No active plan to roll over"))
            is AppResult.Error -> return result
        }

        when (val archived = monthlyPlanRepository.archivePlan(activePlan.id)) {
            is AppResult.Error -> return archived
            is AppResult.Success -> Unit
        }

        return if (activePlan.source == PlanSource.LOGGED_IN_RULE_ENGINE && fullAssessment != null) {
            val level = when (val result = getLatestQuickAssessmentResult()) {
                is AppResult.Success -> result.data?.level ?: Level.BEGINNER
                is AppResult.Error -> return result
            }
            generateLoggedInPlan(fullAssessment, level)
        } else {
            val quickAssessment = when (val result = getLatestQuickAssessmentResult()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> return result
            } ?: return AppResult.Error(Failure.Database("No Quick Assessment result to regenerate from"))
            generateLocalTemplatePlan(quickAssessment)
        }
    }
}
