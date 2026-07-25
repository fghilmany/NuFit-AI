package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutExerciseLog
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSetLog
import com.fghilmany.nufitai.fake.FakePtModeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class EndSessionEarlyTest {

    @Test
    fun `given remaining unattempted exercises when ended early then they are marked skipped and the session completes`() = runTest {
        val repository = FakePtModeRepository()
        val session = WorkoutSession(
            id = "s1", planDayId = "d1", startedAt = Instant.fromEpochMilliseconds(0), completedAt = null,
            status = WorkoutSessionStatus.IN_PROGRESS, currentExerciseIndex = 1, currentSetNumber = 1, restEndAt = Instant.fromEpochMilliseconds(1000),
        )
        val currentLog = WorkoutExerciseLog(id = "el-current", workoutSessionId = "s1", plannedExerciseIndex = 1, exerciseId = "EX-2", skippedAt = null)
        val futureLog = WorkoutExerciseLog(id = "el-future", workoutSessionId = "s1", plannedExerciseIndex = 2, exerciseId = "EX-3", skippedAt = null)

        val result = EndSessionEarly(repository)(session, exerciseLogsToClose = listOf(currentLog, futureLog))

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(WorkoutSessionStatus.COMPLETED, result.data.status)
        assertNotNull(result.data.completedAt)
        assertNull(result.data.restEndAt)
        assertEquals(2, repository.exerciseLogs.size)
        assertTrue(repository.exerciseLogs.values.all { it.skippedAt != null })
    }

    @Test
    fun `given the current slot already has logged sets when ended early then it is marked skipped in place, not duplicated`() = runTest {
        val repository = FakePtModeRepository()
        val session = WorkoutSession(
            id = "s1", planDayId = "d1", startedAt = Instant.fromEpochMilliseconds(0), completedAt = null,
            status = WorkoutSessionStatus.IN_PROGRESS, currentExerciseIndex = 0, currentSetNumber = 2, restEndAt = null,
        )
        val currentLog = WorkoutExerciseLog(id = "el-current", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = null)
        repository.exerciseLogs[currentLog.id] = currentLog
        repository.setLogs += WorkoutSetLog(id = "set1", workoutExerciseLogId = "el-current", setNumber = 1, weightKg = 20.0, reps = 10, completedAt = Instant.fromEpochMilliseconds(500))

        val result = EndSessionEarly(repository)(session, exerciseLogsToClose = listOf(currentLog))

        assertIs<AppResult.Success<WorkoutSession>>(result)
        assertEquals(1, repository.exerciseLogs.size)
        assertNotNull(repository.exerciseLogs["el-current"]?.skippedAt)
        assertEquals(1, repository.setLogs.size) // the already-logged set is untouched, still attached to the same log id
    }
}
