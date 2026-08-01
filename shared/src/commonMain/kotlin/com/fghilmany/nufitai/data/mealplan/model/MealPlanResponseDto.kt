package com.fghilmany.nufitai.data.mealplan.model

import com.fghilmany.nufitai.core.util.generateId
import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import kotlinx.serialization.Serializable

@Serializable
data class MealPlanResponseDto(val days: List<MealPlanDayDto>)

@Serializable
data class MealPlanDayDto(val dayNumber: Int, val meals: List<PlannedMealDto>)

@Serializable
data class PlannedMealDto(
    val slot: String,
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val macros: MacroTargetDto,
    val prepMinutes: Int,
    val tag: String,
) {
    /** [PlannedMeal.mealPlanId] is stamped by the caller once the (possibly-reused) MealPlan.id is known -- mirrors AiPlanDayDto's PlanDay.id placeholder precedent. */
    fun toEntity(dayNumber: Int, mealPlanId: String): PlannedMeal = PlannedMeal(
        id = generateId(),
        mealPlanId = mealPlanId,
        dayNumber = dayNumber,
        slot = MealSlot.valueOf(slot),
        name = name,
        description = description,
        ingredients = ingredients,
        macros = macros.toEntity(),
        prepMinutes = prepMinutes,
        tag = tag,
    )
}
