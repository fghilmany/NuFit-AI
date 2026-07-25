package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.fake.FakePtModeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class ConfirmSetTest {
    private val start = Instant.fromEpochMilliseconds(0)

    private fun session(currentSetNumber: Int = 1, currentExerciseIndex: Int = 0) = WorkoutSession(
        id = "s1", planDayId = "d1", startedAt = start, completedAt = null, status = WorkoutSessionStatus.IN_PROGRESS,
        currentExerciseIndex = currentExerciseIndex, currentSetNumber = currentSetNumber, restEndAt = null,
    )

    private fun exerciseLog() = WorkoutExerciseLog(id = "el1", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = null)

    @Test
    fun `given a set that is not the last of the exercise when confirmed then advances the set number and starts the rest timer`() = runTest {
        val repository = FakePtModeRepository()
        val result = ConfirmSet(repository)(session(), exerciseLog(), weightKg = 20.0, reps = 10, totalSetsForExercise = 3, restSeconds = 90, isLastExercise = false)

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(2, result.data.currentSetNumber)
        assertEquals(0, result.data.currentExerciseIndex)
        assertNotNull(result.data.restEndAt)
        assertEquals(1, repository.setLogs.size)
    }

    @Test
    fun `given the last set of a non-final exercise when confirmed then advances to the next exercise and rests`() = runTest {
        val repository = FakePtModeRepository()
        val result = ConfirmSet(repository)(session(currentSetNumber = 3), exerciseLog(), weightKg = 20.0, reps = 10, totalSetsForExercise = 3, restSeconds = 90, isLastExercise = false)

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(1, result.data.currentExerciseIndex)
        assertEquals(1, result.data.currentSetNumber)
        assertNotNull(result.data.restEndAt)
    }

    @Test
    fun `given the last set of the last exercise when confirmed then completes the session without resting`() = runTest {
        val repository = FakePtModeRepository()
        val result = ConfirmSet(repository)(session(currentSetNumber = 3), exerciseLog(), weightKg = 20.0, reps = 10, totalSetsForExercise = 3, restSeconds = 90, isLastExercise = true)

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(WorkoutSessionStatus.COMPLETED, result.data.status)
        assertNotNull(result.data.completedAt)
        assertNull(result.data.restEndAt)
    }
}
