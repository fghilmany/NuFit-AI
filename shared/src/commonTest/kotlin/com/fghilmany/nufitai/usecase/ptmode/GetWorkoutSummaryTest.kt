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
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class GetWorkoutSummaryTest {
    private val start = Instant.fromEpochMilliseconds(0)

    private fun session(completedAt: Instant?) = WorkoutSession(
        id = "s1", planDayId = "d1", startedAt = start, completedAt = completedAt, status = WorkoutSessionStatus.COMPLETED,
        currentExerciseIndex = 2, currentSetNumber = 1, restEndAt = null,
    )

    @Test
    fun `given completed sets when invoked then volume is the sum of weight times reps`() = runTest {
        val repository = FakePtModeRepository()
        val exerciseLog = WorkoutExerciseLog(id = "el1", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = null)
        repository.exerciseLogs[exerciseLog.id] = exerciseLog
        repository.setLogs += WorkoutSetLog(id = "set1", workoutExerciseLogId = "el1", setNumber = 1, weightKg = 20.0, reps = 10, completedAt = start + 1.minutes)
        repository.setLogs += WorkoutSetLog(id = "set2", workoutExerciseLogId = "el1", setNumber = 2, weightKg = 20.0, reps = 8, completedAt = start + 2.minutes)

        val result = GetWorkoutSummary(repository)(session(start + 10.minutes), exercisesById = emptyMap(), totalExerciseCount = 1)

        assertIs<AppResult.Success<*>>(result)
        assertEquals(360.0, result.data.totalVolumeKg) // 20*10 + 20*8
        assertEquals(10, result.data.durationMinutes)
        assertEquals(1, result.data.completedExerciseCount)
    }

    @Test
    fun `given a skipped exercise when invoked then it is excluded from the completed count and contributes no volume`() = runTest {
        val repository = FakePtModeRepository()
        val skippedLog = WorkoutExerciseLog(id = "el1", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = start + 1.minutes)
        repository.exerciseLogs[skippedLog.id] = skippedLog

        val result = GetWorkoutSummary(repository)(session(start + 5.minutes), exercisesById = emptyMap(), totalExerciseCount = 2)

        assertIs<AppResult.Success<*>>(result)
        assertEquals(0.0, result.data.totalVolumeKg)
        assertEquals(0, result.data.completedExerciseCount)
    }

    @Test
    fun `given this session's max weight exceeds prior history when invoked then a personal record is emitted`() = runTest {
        val repository = FakePtModeRepository()
        val priorLog = WorkoutExerciseLog(id = "elPrior", workoutSessionId = "sOld", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = null)
        repository.exerciseLogs[priorLog.id] = priorLog
        repository.setLogs += WorkoutSetLog(id = "setPrior", workoutExerciseLogId = "elPrior", setNumber = 1, weightKg = 15.0, reps = 10, completedAt = start - 1.minutes)

        val currentLog = WorkoutExerciseLog(id = "el1", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = null)
        repository.exerciseLogs[currentLog.id] = currentLog
        repository.setLogs += WorkoutSetLog(id = "set1", workoutExerciseLogId = "el1", setNumber = 1, weightKg = 20.0, reps = 10, completedAt = start + 1.minutes)

        val result = GetWorkoutSummary(repository)(session(start + 5.minutes), exercisesById = emptyMap(), totalExerciseCount = 1)

        assertIs<AppResult.Success<*>>(result)
        assertTrue(result.data.personalRecords.any { it.exerciseId == "EX-1" && it.newWeightKg == 20.0 && it.previousBestKg == 15.0 })
    }

    @Test
    fun `given no prior history for an exercise when invoked then it is not treated as a personal record`() = runTest {
        val repository = FakePtModeRepository()
        val currentLog = WorkoutExerciseLog(id = "el1", workoutSessionId = "s1", plannedExerciseIndex = 0, exerciseId = "EX-1", skippedAt = null)
        repository.exerciseLogs[currentLog.id] = currentLog
        repository.setLogs += WorkoutSetLog(id = "set1", workoutExerciseLogId = "el1", setNumber = 1, weightKg = 20.0, reps = 10, completedAt = start + 1.minutes)

        val result = GetWorkoutSummary(repository)(session(start + 5.minutes), exercisesById = emptyMap(), totalExerciseCount = 1)

        assertIs<AppResult.Success<*>>(result)
        assertTrue(result.data.personalRecords.isEmpty())
    }
}
