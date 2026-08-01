package com.fghilmany.nufitai.domain.mealplan.entity

/** P-17 B, singleton per user (one row, upserted, not versioned/historized) -- read by GenerateMealPlan as extra AI context. */
data class MealPreference(
    val cuisineThemes: Set<String>,
    val mood: MealMood?,
    val allergies: List<String>,
    val scheduleMode: MealScheduleMode,
)
