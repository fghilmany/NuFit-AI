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
    val jenis: String,
    val durasiMenit: Int,
    val intensitas: String?,
)

data class WarmupBlock(
    val umum: CardioBlock,
    val spesifik: List<PlannedExercise>,
    val korektif: List<PlannedExercise>,
)

data class CooldownBlock(
    val penurunanHr: CardioBlock,
    val stretch: List<PlannedExercise>,
)
