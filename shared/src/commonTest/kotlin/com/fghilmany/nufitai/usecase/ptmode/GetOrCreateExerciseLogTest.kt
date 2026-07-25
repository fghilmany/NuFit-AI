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
import kotlin.time.Instant

class GetOrCreateExerciseLogTest {
    private val session = WorkoutSession(
        id = "s1", planDayId = "d1", startedAt = Instant.fromEpochMilliseconds(0), completedAt = null,
        status = WorkoutSessionStatus.IN_PROGRESS, currentExerciseIndex = 0, currentSetNumber = 1, restEndAt = null,
    )

    @Test
    fun `given no log exists for the slot when invoked then creates one defaulting to the plan's exercise`() = runTest {
        val repository = FakePtModeRepository()

        val result = GetOrCreateExerciseLog(repository)(session, plannedExerciseIndex = 0, defaultExerciseId = "EX-1")

        assertIs<AppResult.Success<WorkoutExerciseLog>>(result)
        assertEquals("EX-1", result.data.exerciseId)
        assertEquals(1, repository.exerciseLogs.size)
    }

    @Test
    fun `given a log already exists for the slot when invoked then returns it instead of creating a duplicate`() = runTest {
        val repository = FakePtModeRepository()
        val existing = WorkoutExerciseLog(id = "existing", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-2", skippedAt = null)
        repository.exerciseLogs[existing.id] = existing

        val result = GetOrCreateExerciseLog(repository)(session, plannedExerciseIndex = 0, defaultExerciseId = "EX-1")

        assertIs<AppResult.Success<WorkoutExerciseLog>>(result)
        assertEquals("existing", result.data.id)
        assertEquals("EX-2", result.data.exerciseId)
        assertEquals(1, repository.exerciseLogs.size)
    }
}
