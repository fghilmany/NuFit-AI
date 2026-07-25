package com.fghilmany.nufitai.domain.monthlyplan.entity

/**
 * [reasonRuleIds] is the transparency principle from issue #29: every planned exercise
 * carries the rule ID(s) that selected/modified it (e.g. "SAFE-02", "CAL-01"), so "kenapa
 * saya dapat gerakan ini?" is always answerable.
 */
data class PlannedExercise(
    val exerciseId: String,
    val sets: Int,
    val repRangeOrDuration: String,
    val rpeTargetMin: Int,
    val rpeTargetMax: Int,
    val reasonRuleIds: List<String>,
    /** issue #80 -- PT Mode's rest-timer duration; null falls back to a 90s default. */
    val restSeconds: Int? = null,
)

data class CardioBlock(
    val type: String,
    val durationMinutes: Int,
    val intensity: String?,
)

data class WarmupBlock(
    val general: CardioBlock,
    val specific: List<PlannedExercise>,
    val corrective: List<PlannedExercise>,
)

data class CooldownBlock(
    val heartRateCooldown: CardioBlock,
    val stretch: List<PlannedExercise>,
)
