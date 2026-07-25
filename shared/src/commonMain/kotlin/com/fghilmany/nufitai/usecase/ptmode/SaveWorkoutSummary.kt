package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession

/**
 * AC-5/AC-9: writes into the existing PlanDaySessionLog (already consumed unchanged by
 * GenerateNextMonthPlan) so P-03/P-04's SessionStatus keeps working. Uses the WorkoutSession's
 * own id as the log's id so calling this twice (once on P-06 load with rpeReported=null to
 * guarantee AC-9, again on "Simpan" with the chosen RPE) upserts the same row instead of
 * creating a duplicate for the same plan day.
 */
class SaveWorkoutSummary(private val monthlyPlanRepository: MonthlyPlanRepository) {
    suspend operator fun invoke(workoutSession: WorkoutSession, rpeReported: Int?): AppResult<Unit> {
        val log = PlanDaySessionLog(
            id = workoutSession.id,
            planDayId = workoutSession.planDayId,
            completedAt = workoutSession.completedAt ?: currentInstant(),
            skippedAt = null,
            rpeReported = rpeReported,
            painReported = null,
        )
        return monthlyPlanRepository.saveSessionLog(log)
    }
}
