package com.fghilmany.nufitai.usecase.assessmentdetail

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class GetAssessmentHistoryTest {

    private fun plan(id: String, startedAt: Instant, status: PlanStatus) = MonthlyPlan(
        id = id,
        startedAt = startedAt,
        cycleNumber = 1,
        source = PlanSource.LOCAL_TEMPLATE,
        status = status,
        levelMeta = "BEGINNER",
        goalMeta = GoalCategory.FAT_LOSS,
        smartGoalMeta = null,
        flagsAktif = emptySet(),
        startingLevelPerPola = emptyMap(),
        mode = ProgressionMode.NORMAL,
        checkpointDays = emptyList(),
    )

    @Test
    fun `given multiple cycles when invoked then returns them newest first`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        repository.allPlans = mutableListOf(
            plan("p1", Instant.fromEpochMilliseconds(0), PlanStatus.ARCHIVED),
            plan("p2", Instant.fromEpochMilliseconds(1_000), PlanStatus.ACTIVE),
        )
        val getAssessmentHistory = GetAssessmentHistory(repository)

        val result = getAssessmentHistory()

        assertIs<AppResult.Success<List<MonthlyPlan>>>(result)
        assertEquals(listOf("p2", "p1"), result.data.map { it.id })
    }

    @Test
    fun `given no plans yet when invoked then returns an empty list`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        val getAssessmentHistory = GetAssessmentHistory(repository)

        val result = getAssessmentHistory()

        assertIs<AppResult.Success<List<MonthlyPlan>>>(result)
        assertEquals(emptyList(), result.data)
    }
}
