package com.fghilmany.nufitai.domain.fullassessment.entity

/** techspec meal-plan.md §5 -- TDEE activity multiplier (Mifflin-St Jeor). */
enum class ActivityLevel(val tdeeMultiplier: Double) {
    SEDENTARY(1.2),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.9),
}
