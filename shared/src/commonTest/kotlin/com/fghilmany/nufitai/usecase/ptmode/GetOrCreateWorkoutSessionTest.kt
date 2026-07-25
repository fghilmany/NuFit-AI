package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.fake.FakePtModeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class GetOrCreateWorkoutSessionTest {

    @Test
    fun `given no existing session when invoked then creates a new IN_PROGRESS session starting at day one set one`() = runTest {
        val repository = FakePtModeRepository()

        val result = GetOrCreateWorkoutSession(repository)("d1")

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(WorkoutSessionStatus.IN_PROGRESS, result.data.status)
        assertEquals(0, result.data.currentExerciseIndex)
        assertEquals(1, result.data.currentSetNumber)
        assertEquals(1, repository.sessions.size)
    }

    @Test
    fun `given an existing IN_PROGRESS session for the day when invoked then resumes it instead of creating a duplicate`() = runTest {
        val repository = FakePtModeRepository()
        val existing = WorkoutSession(
            id = "existing", planDayId = "d1", startedAt = Instant.fromEpochMilliseconds(0), completedAt = null,
            status = WorkoutSessionStatus.IN_PROGRESS, currentExerciseIndex = 2, currentSetNumber = 2, restEndAt = null,
        )
        repository.sessions[existing.id] = existing

        val result = GetOrCreateWorkoutSession(repository)("d1")

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals("existing", result.data.id)
        assertEquals(1, repository.sessions.size)
    }
}
