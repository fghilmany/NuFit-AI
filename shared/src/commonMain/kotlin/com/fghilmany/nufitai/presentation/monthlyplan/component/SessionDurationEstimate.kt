package com.fghilmany.nufitai.presentation.monthlyplan.component

import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay

/** DoD: "Estimasi durasi tampil di setiap kartu sesi" -- rough estimate, not a stored value. */
fun PlanDay.estimatedDurationMinutes(): Int {
    val warmupMinutes = (warmup?.general?.durationMinutes ?: 0) +
        (warmup?.specific?.size ?: 0) +
        (warmup?.corrective?.size ?: 0) * 2
    val mainMinutes = (mainExercises?.sumOf { it.sets } ?: 0) * 2 // ~2 min/set incl. rest
    val cardioMinutes = cardio?.durationMinutes ?: 0
    val cooldownMinutes = (cooldown?.heartRateCooldown?.durationMinutes ?: 0) + (cooldown?.stretch?.size ?: 0)
    return warmupMinutes + mainMinutes + cardioMinutes + cooldownMinutes
}

fun PlanDay.exerciseCount(): Int = mainExercises?.size ?: 0
