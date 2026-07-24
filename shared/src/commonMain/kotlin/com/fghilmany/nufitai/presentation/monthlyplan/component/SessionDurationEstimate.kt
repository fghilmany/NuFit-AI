package com.fghilmany.nufitai.presentation.monthlyplan.component

import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay

/** DoD: "Estimasi durasi tampil di setiap kartu sesi" -- rough estimate, not a stored value. */
fun PlanDay.estimatedDurationMinutes(): Int {
    val warmupMinutes = (warmup?.umum?.durasiMenit ?: 0) +
        (warmup?.spesifik?.size ?: 0) +
        (warmup?.korektif?.size ?: 0) * 2
    val mainMinutes = (mainExercises?.sumOf { it.sets } ?: 0) * 2 // ~2 min/set incl. rest
    val cardioMinutes = cardio?.durasiMenit ?: 0
    val cooldownMinutes = (cooldown?.penurunanHr?.durasiMenit ?: 0) + (cooldown?.stretch?.size ?: 0)
    return warmupMinutes + mainMinutes + cardioMinutes + cooldownMinutes
}

fun PlanDay.exerciseCount(): Int = mainExercises?.size ?: 0
