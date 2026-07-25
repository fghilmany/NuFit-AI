package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/** AC-1: rest timer can be skipped early. */
class SkipRestTimer(private val repository: PtModeRepository) {
    suspend operator fun invoke(session: WorkoutSession): AppResult<WorkoutSession> {
        val updated = session.copy(restEndAt = null)
        return when (val result = repository.updateSessionProgress(updated)) {
            is AppResult.Error -> result
            is AppResult.Success -> AppResult.Success(updated)
        }
    }
}
