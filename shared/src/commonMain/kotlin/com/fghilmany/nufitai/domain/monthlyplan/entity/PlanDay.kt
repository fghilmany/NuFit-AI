package com.fghilmany.nufitai.domain.monthlyplan.entity

data class PlanDay(
    val id: String,
    val planId: String,
    val dayNumber: Int,
    val type: DayType,
    val templateLetter: String?,
    val warmup: WarmupBlock?,
    val mainExercises: List<PlannedExercise>?,
    val cardio: CardioBlock?,
    val cooldown: CooldownBlock?,
    /** Corrective overflow (SAFE-09) -- only ever populated on REST days. */
    val homework: List<PlannedExercise>?,
)
