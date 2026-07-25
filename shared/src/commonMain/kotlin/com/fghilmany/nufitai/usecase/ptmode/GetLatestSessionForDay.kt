package com.fghilmany.nufitai.usecase.ptmode

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.ptmode.entity.WorkoutSession
import com.fghilmany.nufitai.domain.ptmode.repository.PtModeRepository

/** P-06 looks its just-finished (or previously finished) session up by planDayId. */
class GetLatestSessionForDay(private val repository: PtModeRepository) {
    suspend operator fun invoke(planDayId: String): AppResult<WorkoutSession?> = repository.getLatestSessionForDay(planDayId)
}
