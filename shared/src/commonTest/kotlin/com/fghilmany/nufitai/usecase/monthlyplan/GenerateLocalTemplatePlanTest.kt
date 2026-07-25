package com.fghilmany.nufitai.usecase.monthlyplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.FakeMonthlyPlanRepository
import com.fghilmany.nufitai.fake.testExercise
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class GenerateLocalTemplatePlanTest {

    private val bodyweightPatterns = listOf(
        MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL,
        MovementPattern.PUSH_VERTICAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.PULL_VERTICAL, MovementPattern.CORE,
    )

    private fun quickAssessment(resolvedSplit: ResolvedSplit) = QuickAssessmentResult(
        id = "qa1",
        answeredAt = Instant.fromEpochMilliseconds(0),
        input = QuickAssessmentAnswer(
            experience = Experience.NEVER,
            goal = GoalCategory.FAT_LOSS,
            equipment = setOf(EquipmentCategory.BODYWEIGHT),
            frequency = FrequencyBucket.THREE,
            splitPreference = SplitPreference.FULL_BODY,
        ),
        level = Level.BEGINNER,
        resolvedSplit = resolvedSplit,
        splitEducationalNote = null,
        templateId = "T1",
    )

    @Test
    fun `given a beginner full-body quick assessment when invoked then produces a 30-day plan with the expected session count`() = runTest {
        val exerciseRepo = FakeExerciseLibraryRepository().apply {
            exercises = bodyweightPatterns.map { testExercise("BW-${it.name}-000", movementPattern = it) }
        }
        val planRepo = FakeMonthlyPlanRepository()
        val generate = GenerateLocalTemplatePlan(GetExercisePool(exerciseRepo), planRepo)

        val result = generate(quickAssessment(ResolvedSplit.FULL_BODY))

        assertIs<AppResult.Success<*>>(result)
        assertEquals(30, planRepo.savedDays.size)
        val sessionDays = planRepo.savedDays.filter { it.type == DayType.SESSION }
        assertEquals(12, sessionDays.size) // floor(3 * 30 / 7) = 12
        sessionDays.forEach { day ->
            assert(!day.mainExercises.isNullOrEmpty()) { "day ${day.dayNumber} should have main exercises" }
        }
    }
}
