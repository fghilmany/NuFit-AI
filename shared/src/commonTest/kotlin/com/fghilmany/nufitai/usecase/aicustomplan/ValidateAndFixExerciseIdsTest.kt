package com.fghilmany.nufitai.usecase.aicustomplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.aicustomplan.entity.ValidatedPlanDraft
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

class ValidateAndFixExerciseIdsTest {

    @Test
    fun `given all valid ids when invoked then all exercises pass through untouched`() = runTest {
        val library = FakeExerciseLibraryRepository().apply { exercises = listOf(testExercise("BW-SQUAT-001")) }
        val validate = ValidateAndFixExerciseIds(library)

        val result = validate(listOf(planDay(1, listOf("BW-SQUAT-001"))))

        assertIs<AppResult.Success<ValidatedPlanDraft>>(result)
        assertEquals(1, result.data.days.single().mainExercises?.size)
        assertTrue(result.data.warnings.isEmpty())
    }

    @Test
    fun `given an unknown exercise id when invoked then it is dropped and a warning is surfaced`() = runTest {
        val library = FakeExerciseLibraryRepository().apply { exercises = listOf(testExercise("BW-SQUAT-001")) }
        val validate = ValidateAndFixExerciseIds(library)

        val result = validate(listOf(planDay(1, listOf("BW-SQUAT-001", "PHANTOM-999"))))

        assertIs<AppResult.Success<ValidatedPlanDraft>>(result)
        assertEquals(listOf("BW-SQUAT-001"), result.data.days.single().mainExercises?.map { it.exerciseId })
        assertEquals(1, result.data.warnings.size)
        assertEquals("PHANTOM-999", result.data.warnings.single().invalidExerciseId)
        assertEquals(1, result.data.warnings.single().dayNumber)
    }
}
