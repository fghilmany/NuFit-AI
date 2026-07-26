package com.fghilmany.nufitai.usecase.planmanagement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private fun plan(id: String, status: PlanStatus, source: PlanSource = PlanSource.LOCAL_TEMPLATE) = MonthlyPlan(
    id = id,
    startedAt = currentInstant(),
    cycleNumber = 1,
    source = source,
    status = status,
    levelMeta = "BEGINNER",
    goalMeta = GoalCategory.MUSCLE_GAIN,
    smartGoalMeta = null,
    activeFlags = emptySet(),
    startingLevelPerPattern = emptyMap(),
    mode = ProgressionMode.NORMAL,
    checkpointDays = emptyList(),
)

class DeletePlanTest {

    @Test
    fun `given a DRAFT template plan when invoked then it is soft deleted`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply { allPlans.add(plan("draft-1", PlanStatus.DRAFT)) }
        val deletePlan = DeletePlan(repository)

        val result = deletePlan("draft-1")

        assertIs<AppResult.Success<Unit>>(result)
        // Soft delete: the row survives (never a physical DELETE), just marked and excluded from getAllPlans().
        assertNotNull(repository.allPlans.single { it.id == "draft-1" }.deletedAt)
        assertTrue((repository.getAllPlans() as AppResult.Success).data.none { it.id == "draft-1" })
    }

    @Test
    fun `given an ARCHIVED AI plan when invoked then it is soft deleted (not just AI drafts)`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            allPlans.add(plan("archived-ai", PlanStatus.ARCHIVED, source = PlanSource.AI_GENERATED))
        }
        val deletePlan = DeletePlan(repository)

        val result = deletePlan("archived-ai")

        assertIs<AppResult.Success<Unit>>(result)
    }

    @Test
    fun `given an ACTIVE plan of any source when invoked then deletion is rejected`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply { allPlans.add(plan("active-1", PlanStatus.ACTIVE)) }
        val deletePlan = DeletePlan(repository)

        val result = deletePlan("active-1")

        assertIs<AppResult.Error>(result)
    }

    @Test
    fun `given an already deleted plan id when invoked then it is idempotent`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        val deletePlan = DeletePlan(repository)

        val result = deletePlan("does-not-exist")

        assertIs<AppResult.Success<Unit>>(result)
    }

    @Test
    fun `given a deleted plan when invoked then its plan_day and session logs are never touched`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            allPlans.add(plan("draft-1", PlanStatus.DRAFT))
            planDays["draft-1"] = listOf(
                PlanDay(id = "day-1", planId = "draft-1", dayNumber = 1, type = DayType.SESSION, templateLetter = "A", warmup = null, mainExercises = null, cardio = null, cooldown = null, homework = null),
            )
            sessionLogs["draft-1"] = listOf(PlanDaySessionLog(id = "log-1", planDayId = "day-1", completedAt = currentInstant(), skippedAt = null, rpeReported = 7, painReported = null))
        }
        val deletePlan = DeletePlan(repository)

        val result = deletePlan("draft-1")

        assertIs<AppResult.Success<Unit>>(result)
        // AC-5 -- log history physically survives the delete, proven by direct query (not exposed via getAllPlans).
        assertTrue(repository.planDays["draft-1"]?.isNotEmpty() == true)
        assertTrue(repository.sessionLogs["draft-1"]?.isNotEmpty() == true)
    }
}
