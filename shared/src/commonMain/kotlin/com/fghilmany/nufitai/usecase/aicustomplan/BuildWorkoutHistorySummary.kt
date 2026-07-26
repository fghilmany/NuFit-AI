package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.aicustomplan.entity.WorkoutHistorySummary
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.repository.MonthlyPlanRepository

private val EMPTY_SUMMARY = WorkoutHistorySummary(cycleCount = 0, attendanceRate = 0f, averageRpe = null, completedSessionCount = 0)

/** techspec §5/§9 item 7 -- compact AI context, sourced from the active plan's own session logs, if any. */
class BuildWorkoutHistorySummary(private val repository: MonthlyPlanRepository) {
    suspend operator fun invoke(): AppResult<WorkoutHistorySummary> {
        val plan = when (val result = repository.getActivePlan()) {
            is AppResult.Success -> result.data ?: return AppResult.Success(EMPTY_SUMMARY)
            is AppResult.Error -> return result
        }
        val days = when (val result = repository.getPlanDays(plan.id)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }
        val logs = when (val result = repository.getSessionLogsForPlan(plan.id)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return result
        }

        val sessionDayCount = days.count { it.type == DayType.SESSION }
        val completedCount = logs.count { it.completedAt != null }
        val attendanceRate = if (sessionDayCount == 0) 0f else completedCount.toFloat() / sessionDayCount
        val reportedRpes = logs.mapNotNull { it.rpeReported }
        val averageRpe = if (reportedRpes.isEmpty()) null else reportedRpes.average().toFloat()

        return AppResult.Success(
            WorkoutHistorySummary(
                cycleCount = plan.cycleNumber,
                attendanceRate = attendanceRate,
                averageRpe = averageRpe,
                completedSessionCount = completedCount,
            ),
        )
    }
}
