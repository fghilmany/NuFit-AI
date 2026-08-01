package com.fghilmany.nufitai.domain.mealplan.entity

data class PlannedMeal(
    val id: String,
    val mealPlanId: String,
    val dayNumber: Int,       // 1..7
    val slot: MealSlot,
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val macros: MacroTarget,
    /** Screenshot's "15 mnt" pill -- prep/cook time, not related to macros. */
    val prepMinutes: Int,
    /** Screenshot's single descriptive pill ("Nusantara" / "Tinggi Serat" / "Tinggi Protein") -- AI-provided free text, not a fixed enum (mixes cuisine and nutrition descriptors). */
    val tag: String,
)
