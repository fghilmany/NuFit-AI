package com.fghilmany.nufitai.usecase.chatai

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.chatai.repository.ChatExerciseOverrideRepository
import com.fghilmany.nufitai.usecase.monthlyplan.GetActivePlanOverview
import com.fghilmany.nufitai.usecase.monthlyplan.GetSessionStatus
import com.fghilmany.nufitai.usecase.monthlyplan.SessionStatus

/**
 * techspec chat-ai.md §5/§7/§9 item 12 (BLOCKING, AC-4) -- "Terapkan ke Plan". Finds the nearest
 * TODAY/UPCOMING day that still contains [replacesExerciseId] and writes a pending,
 * single-use override for that exact slot -- mirrors PT Mode's `SwapExercise` semantics
 * ("plan asli tidak berubah untuk sesi berikutnya"): the `PlanDay.mainExercises` template
 * itself is never touched, only consumed once by `GetOrCreateExerciseLog`.
 */
class ApplyChatExerciseSuggestion(
    private val getActivePlanOverview: GetActivePlanOverview,
    private val getSessionStatus: GetSessionStatus,
    private val overrideRepository: ChatExerciseOverrideRepository,
) {
    suspend operator fun invoke(replacesExerciseId: String, substituteExerciseId: String): AppResult<Unit> {
        val overview = when (val result = getActivePlanOverview()) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data
                ?: return AppResult.Error(Failure.Validation("Tidak ada plan aktif"))
        }

        val now = currentInstant()
        val targetDay = overview.days
            .filter { day -> day.mainExercises.orEmpty().any { it.exerciseId == replacesExerciseId } }
            .filter { day ->
                val log = overview.logs.find { it.planDayId == day.id }
                val status = getSessionStatus(day, log, overview.plan.startedAt, now)
                status == SessionStatus.TODAY || status == SessionStatus.UPCOMING
            }
            .minByOrNull { it.dayNumber }
            ?: return AppResult.Error(Failure.Validation("Exercise tidak ditemukan di sesi mendatang kamu"))

        val plannedExerciseIndex = targetDay.mainExercises.orEmpty().indexOfFirst { it.exerciseId == replacesExerciseId }
        return overrideRepository.createOverride(targetDay.id, plannedExerciseIndex, substituteExerciseId)
    }
}
