package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.testExercise
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private fun plannedExercise(exerciseId: String) = PlannedExercise(
    exerciseId = exerciseId,
    sets = 3,
    repRangeOrDuration = "8-12",
    rpeTargetMin = 6,
    rpeTargetMax = 8,
    reasonRuleIds = listOf("AI_GENERATED"),
)

private fun planDay(dayNumber: Int, exerciseIds: List<String>) = PlanDay(
    id = "day-$dayNumber",
    planId = "",
    dayNumber = dayNumber,
    type = DayType.SESSION,
    templateLetter = "A",
    warmup = null,
    mainExercises = exerciseIds.map { plannedExercise(it) },
    cardio = null,
    cooldown = null,
    homework = null,
)

class CheckInjuryConflictsTest {

    @Test
    fun `given no overlap with active flags or injury areas when invoked then no conflicts`() = runTest {
        val library = FakeExerciseLibraryRepository().apply {
            exercises = listOf(testExercise("BW-SQUAT-001"))
        }
        val check = CheckInjuryConflicts(library)

        val result = check(listOf(planDay(1, listOf("BW-SQUAT-001"))), activeFlags = emptySet(), injuryHistory = emptySet())

        assertIs<AppResult.Success<List<com.fghilmany.nufitai.domain.aicustomplan.entity.InjuryConflict>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `given exercise flagExclusion matches an active health flag when invoked then reports conflict`() = runTest {
        val library = FakeExerciseLibraryRepository().apply {
            exercises = listOf(testExercise("BW-SQUAT-001", flagExclusion = setOf(ExerciseFlag.HEALTH_JOINT)))
        }
        val check = CheckInjuryConflicts(library)

        val result = check(listOf(planDay(1, listOf("BW-SQUAT-001"))), activeFlags = setOf(ExerciseFlag.HEALTH_JOINT), injuryHistory = emptySet())

        assertIs<AppResult.Success<List<com.fghilmany.nufitai.domain.aicustomplan.entity.InjuryConflict>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("BW-SQUAT-001", result.data.single().exerciseId)
    }

    @Test
    fun `given exercise loadedBodyAreas matches injury history when invoked then reports conflict`() = runTest {
        val library = FakeExerciseLibraryRepository().apply {
            exercises = listOf(testExercise("BW-SQUAT-001", loadedBodyAreas = setOf(BodyArea.KNEE)))
        }
        val check = CheckInjuryConflicts(library)

        val result = check(listOf(planDay(1, listOf("BW-SQUAT-001"))), activeFlags = emptySet(), injuryHistory = setOf(BodyArea.KNEE))

        assertIs<AppResult.Success<List<com.fghilmany.nufitai.domain.aicustomplan.entity.InjuryConflict>>>(result)
        assertEquals(1, result.data.size)
    }
}
