package com.fghilmany.nufitai.usecase.assessmentdetail

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.entity.ParQResult
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestBodyMeasurement
import com.fghilmany.nufitai.usecase.onboarding.GetLatestParQResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult

/** Presentation-facing aggregate for P-09, mirrors GetActivePlanOverview's PlanOverview -- composed, not persisted.
 * [quickAssessment] is nullable -- Full Assessment is a superset (age/gender/goal(s)/equipment/
 * frequency/PAR-Q and more), so a Logged-In user who already completed it should never be forced
 * to redo the lighter Quick Assessment just to unlock this screen. */
data class AssessmentDetailSummary(
    val quickAssessment: QuickAssessmentResult?,
    val parQResult: ParQResult?,
    val activePlan: MonthlyPlan,
    val fullAssessment: FullAssessmentResult?,
    val bodyMeasurement: BodyMeasurement?,
    /** AC-4-adjacent safety check: retake is blocked while a session is in progress (see class doc). */
    val hasInProgressSession: Boolean,
)

/**
 * UC-1 (03-assessment-detail.md, issue #77): composes the Quick Assessment result, PAR-Q flags,
 * active plan, and (when present) Full Assessment data into one read for P-09.
 *
 * `hasInProgressSession` is a forward-compatibility check: PT Mode (05-pt-mode.md) doesn't exist
 * yet, so no `PlanDaySessionLog` row is ever written before a session finishes -- this always
 * evaluates false today, but the check is real (any log row with neither `completedAt` nor
 * `skippedAt` set) so retake stays safe once PT Mode starts persisting session-start state.
 */
class GetAssessmentDetail(
    private val getLatestQuickAssessmentResult: GetLatestQuickAssessmentResult,
    private val getLatestParQResult: GetLatestParQResult,
    private val getLatestFullAssessmentResult: GetLatestFullAssessmentResult,
    private val getLatestBodyMeasurement: GetLatestBodyMeasurement,
    private val monthlyPlanRepository: MonthlyPlanRepository,
) {
    suspend operator fun invoke(): AppResult<AssessmentDetailSummary> {
        val quickAssessment = when (val result = getLatestQuickAssessmentResult()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }

        val fullAssessment = when (val result = getLatestFullAssessmentResult()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }

        // Validation, not Database -- decision #13 lets a plan be created from a template with
        // no assessment at all, so having neither is an expected, recoverable gap (user just
        // hasn't done one yet), not a data-integrity issue. Full Assessment alone is enough --
        // it's a superset of Quick Assessment, never require both.
        if (quickAssessment == null && fullAssessment == null) {
            return AppResult.Error(Failure.Validation("No assessment result found"))
        }

        val parQResult = when (val result = getLatestParQResult()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }

        val activePlan = when (val result = monthlyPlanRepository.getActivePlan()) {
            is AppResult.Success -> result.data
                ?: return AppResult.Error(Failure.Database("No active plan found"))
            is AppResult.Error -> return result
        }

        val bodyMeasurement = when (val result = getLatestBodyMeasurement()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }

        val sessionLogs = when (val result = monthlyPlanRepository.getSessionLogsForPlan(activePlan.id)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val hasInProgressSession = sessionLogs.any { it.completedAt == null && it.skippedAt == null }

        return AppResult.Success(
            AssessmentDetailSummary(
                quickAssessment = quickAssessment,
                parQResult = parQResult,
                activePlan = activePlan,
                fullAssessment = fullAssessment,
                bodyMeasurement = bodyMeasurement,
                hasInProgressSession = hasInProgressSession,
            ),
        )
    }
}
