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

class RetakeAssessmentTest {

    private fun plan(source: PlanSource) = MonthlyPlan(
        id = "p1",
        startedAt = Instant.fromEpochMilliseconds(0),
        cycleNumber = 1,
        source = source,
        status = PlanStatus.ACTIVE,
        levelMeta = "BEGINNER",
        goalMeta = GoalCategory.FAT_LOSS,
        smartGoalMeta = null,
        flagsAktif = emptySet(),
        startingLevelPerPola = emptyMap(),
        mode = ProgressionMode.NORMAL,
        checkpointDays = emptyList(),
    )

    @Test
    fun `given an active local template plan when invoked then archives it and reports its source`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        repository.activePlan = plan(PlanSource.LOCAL_TEMPLATE)
        val retakeAssessment = RetakeAssessment(repository)

        val result = retakeAssessment()

        assertIs<AppResult.Success<PlanSource>>(result)
        assertEquals(PlanSource.LOCAL_TEMPLATE, result.data)
        assertEquals(listOf("p1"), repository.archivedPlanIds)
        assertEquals(PlanStatus.ARCHIVED, repository.activePlan?.status)
    }

    @Test
    fun `given an active logged-in plan when invoked then archives it and reports its source`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        repository.activePlan = plan(PlanSource.LOGGED_IN_RULE_ENGINE)
        val retakeAssessment = RetakeAssessment(repository)

        val result = retakeAssessment()

        assertIs<AppResult.Success<PlanSource>>(result)
        assertEquals(PlanSource.LOGGED_IN_RULE_ENGINE, result.data)
        assertEquals(listOf("p1"), repository.archivedPlanIds)
    }

    @Test
    fun `given no active plan when invoked then returns an error without archiving anything`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        val retakeAssessment = RetakeAssessment(repository)

        val result = retakeAssessment()

        assertIs<AppResult.Error>(result)
        assertEquals(emptyList(), repository.archivedPlanIds)
    }
}
