package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository
import com.fghilmany.nufitai.usecase.monthlyplan.rules.GatingProgresi
import kotlin.time.Instant

/**
 * AC-9/PROG-04: user absent > 14 days -> Home offers a light re-assessment; the calendar itself
 * is never auto-shifted (checked days stay where they are). This usecase only answers "should
 * the offer be shown" -- HomeViewModel (Stage 4) renders the offer card, no date mutation here.
 */
class FreezeCalendarOnAbsence(private val monthlyPlanRepository: MonthlyPlanRepository) {
    suspend operator fun invoke(now: Instant): AppResult<Boolean> {
        val activePlan = when (val result = monthlyPlanRepository.getActivePlan()) {
            is AppResult.Success -> result.data ?: return AppResult.Success(false)
            is AppResult.Error -> return result
        }
        val logs = when (val result = monthlyPlanRepository.getSessionLogsForPlan(activePlan.id)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val lastSessionAt = logs.mapNotNull { it.completedAt }.maxOrNull()
        return AppResult.Success(GatingProgresi.isAbsent(lastSessionAt, now))
    }
}
