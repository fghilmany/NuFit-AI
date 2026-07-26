package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

/**
 * P-14 active-plan card progress bar -- completed/total session days, same math as
 * `usecase/aicustomplan/BuildWorkoutHistorySummary`'s `attendanceRate` (reused, not duplicated,
 * just against an arbitrary [planId] rather than only the active plan).
 */
class GetPlanProgress(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(planId: String): AppResult<Int> {
        val days = when (val result = repository.getPlanDays(planId)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val logs = when (val result = repository.getSessionLogsForPlan(planId)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }

        val sessionDayCount = days.count { it.type == DayType.SESSION }
        val completedCount = logs.count { it.completedAt != null }
        val progressPercent = if (sessionDayCount == 0) 0 else (completedCount * 100) / sessionDayCount

        return AppResult.Success(progressPercent)
    }
}
