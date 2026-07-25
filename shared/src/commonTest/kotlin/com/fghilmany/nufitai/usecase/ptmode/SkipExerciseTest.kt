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

class SkipExerciseTest {
    private val start = Instant.fromEpochMilliseconds(0)

    private fun session(currentExerciseIndex: Int = 0) = WorkoutSession(
        id = "s1", planDayId = "d1", startedAt = start, completedAt = null, status = WorkoutSessionStatus.IN_PROGRESS,
        currentExerciseIndex = currentExerciseIndex, currentSetNumber = 2, restEndAt = null,
    )

    private fun exerciseLog() = WorkoutExerciseLog(id = "el1", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = null)

    @Test
    fun `given a non-final exercise when skipped then marks it skipped and advances to the next exercise`() = runTest {
        val repository = FakePtModeRepository()

        val result = SkipExercise(repository)(session(), exerciseLog(), isLastExercise = false)

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(1, result.data.currentExerciseIndex)
        assertEquals(1, result.data.currentSetNumber)
        assertEquals(WorkoutSessionStatus.IN_PROGRESS, result.data.status)
        assertNotNull(repository.exerciseLogs["el1"]?.skippedAt)
    }

    @Test
    fun `given the last exercise when skipped then the session completes instead of advancing out of bounds`() = runTest {
        val repository = FakePtModeRepository()

        val result = SkipExercise(repository)(session(currentExerciseIndex = 5), exerciseLog(), isLastExercise = true)

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(WorkoutSessionStatus.COMPLETED, result.data.status)
        assertNotNull(result.data.completedAt)
        assertNull(result.data.restEndAt)
    }
}
