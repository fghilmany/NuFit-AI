package com.fghilmany.nufitai.domain.mealplan.entity

import kotlin.time.Instant

data class MealLog(
    val id: String,
    val plannedMealId: String,
    val status: MealLogStatus,
    val loggedAt: Instant?,
)
