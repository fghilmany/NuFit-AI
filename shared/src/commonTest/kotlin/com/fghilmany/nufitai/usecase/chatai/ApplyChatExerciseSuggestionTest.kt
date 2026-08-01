package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.fake.FakeChatExerciseOverrideRepository
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import com.fghilmany.nufitai.usecase.monthlyplan.GetActivePlanOverview
import com.fghilmany.nufitai.usecase.monthlyplan.GetSessionStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class ApplyChatExerciseSuggestionTest {
    private val plan = MonthlyPlan(
        id = "p1", startedAt = Instant.fromEpochMilliseconds(0), cycleNumber = 1, source = PlanSource.LOCAL_TEMPLATE,
        status = PlanStatus.ACTIVE, levelMeta = "BEGINNER", goalMeta = GoalCategory.FAT_LOSS, smartGoalMeta = null,
        activeFlags = emptySet(), startingLevelPerPattern = emptyMap(), mode = ProgressionMode.NORMAL, checkpointDays = listOf(14, 21),
    )

    private fun day(dayNumber: Int, exerciseId: String) = PlanDay(
        id = "d$dayNumber", planId = "p1", dayNumber = dayNumber, type = DayType.SESSION, templateLetter = "A",
        warmup = null, cardio = null, cooldown = null, homework = null,
        mainExercises = listOf(
            PlannedExercise(exerciseId = exerciseId, sets = 3, repRangeOrDuration = "8-12", rpeTargetMin = 6, rpeTargetMax = 8, reasonRuleIds = emptyList()),
        ),
    )

    private fun buildUseCase(repository: FakeMonthlyPlanRepository, overrideRepository: FakeChatExerciseOverrideRepository) = ApplyChatExerciseSuggestion(
        getActivePlanOverview = GetActivePlanOverview(repository),
        getSessionStatus = GetSessionStatus(),
        overrideRepository = overrideRepository,
    )

    @Test
    fun `given the exercise exists in an upcoming day when invoked then it writes an override for that exact slot`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            activePlan = plan
            planDays["p1"] = listOf(day(dayNumber = 1, exerciseId = "PUSH-UP"))
        }
        val overrideRepository = FakeChatExerciseOverrideRepository()

        val result = buildUseCase(repository, overrideRepository)("PUSH-UP", "INCLINE-PUSH-UP")

        assertIs<AppResult.Success<Unit>>(result)
        assertEquals("INCLINE-PUSH-UP", overrideRepository.overrides["d1:0"])
    }

    @Test
    fun `given the exercise is not in any upcoming day when invoked then it returns an error and writes nothing`() = runTest {
        val repository = FakeMonthlyPlanRepository().apply {
            activePlan = plan
            planDays["p1"] = listOf(day(dayNumber = 1, exerciseId = "SQUAT"))
        }
        val overrideRepository = FakeChatExerciseOverrideRepository()

        val result = buildUseCase(repository, overrideRepository)("PUSH-UP", "INCLINE-PUSH-UP")

        assertIs<AppResult.Error>(result)
        assertEquals(emptyMap(), overrideRepository.overrides)
    }

    @Test
    fun `given no active plan when invoked then it returns an error`() = runTest {
        val repository = FakeMonthlyPlanRepository()
        val overrideRepository = FakeChatExerciseOverrideRepository()

        val result = buildUseCase(repository, overrideRepository)("PUSH-UP", "INCLINE-PUSH-UP")

        assertIs<AppResult.Error>(result)
    }
}
