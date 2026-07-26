package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private fun sessionDay(dayNumber: Int) = PlanDay(
    id = "day-$dayNumber",
    planId = "plan-1",
    dayNumber = dayNumber,
    type = DayType.SESSION,
    templateLetter = "A",
    warmup = null,
    mainExercises = null,
    cardio = null,
    cooldown = null,
    homework = null,
)

class GetPlanProgressTest {

    @Test
    fun `given 2 of 4 sessions completed when invoked then progress is 50 percent`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            planDays["plan-1"] = listOf(sessionDay(1), sessionDay(2), sessionDay(3), sessionDay(4))
            sessionLogs["plan-1"] = listOf(
                PlanDaySessionLog(id = "log-1", planDayId = "day-1", completedAt = currentInstant(), skippedAt = null, rpeReported = null, painReported = null),
                PlanDaySessionLog(id = "log-2", planDayId = "day-2", completedAt = currentInstant(), skippedAt = null, rpeReported = null, painReported = null),
            )
        }
        val getPlanProgress = GetPlanProgress(repository)

        val result = getPlanProgress("plan-1")

        assertIs<AppResult.Success<Int>>(result)
        assertEquals(50, result.data)
    }

    @Test
    fun `given no session days when invoked then progress is 0 percent`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        val getPlanProgress = GetPlanProgress(repository)

        val result = getPlanProgress("plan-1")

        assertIs<AppResult.Success<Int>>(result)
        assertEquals(0, result.data)
    }
}
