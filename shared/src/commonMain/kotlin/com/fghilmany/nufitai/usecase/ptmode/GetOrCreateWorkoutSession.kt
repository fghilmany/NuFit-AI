package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSessionStatus
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/** AC-2/AC-7: resumes an existing IN_PROGRESS session for [planDayId], or starts a new one. */
class GetOrCreateWorkoutSession(private val repository: PtModeRepository) {
    suspend operator fun invoke(planDayId: String): AppResult<WorkoutSession> {
        val existing = when (val result = repository.getInProgressSession(planDayId)) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data
        }
        if (existing != null) return AppResult.Success(existing)

        val newSession = WorkoutSession(
            id = generateId(),
            planDayId = planDayId,
            startedAt = currentInstant(),
            completedAt = null,
            status = WorkoutSessionStatus.IN_PROGRESS,
            currentExerciseIndex = 0,
            currentSetNumber = 1,
            restEndAt = null,
        )
        return when (val result = repository.createSession(newSession)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(newSession)
        }
    }
}
