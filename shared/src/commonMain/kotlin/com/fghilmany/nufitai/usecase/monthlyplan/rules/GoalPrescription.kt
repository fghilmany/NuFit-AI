package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory

data class Prescription(
    val loadRatioPercent: Int,
    val cardioRatioPercent: Int,
    val repRange: IntRange,
    val restSecondsRange: IntRange,
    val structureNote: String,
)

/** issue #29 layer 4 -- pure mapping table, no I/O. */
object GoalPrescription {
    operator fun invoke(goal: GoalCategory): Prescription = when (goal) {
        GoalCategory.FAT_LOSS -> Prescription(60, 40, 10..15, 45..60, "compound full-body, circuit ringan")
        GoalCategory.MUSCLE_GAIN -> Prescription(80, 20, 8..12, 60..90, "volume lebih tinggi per grup otot")
        GoalCategory.STRENGTH -> Prescription(90, 10, 6..10, 90..120, "beban fokus, rep rendah")
        GoalCategory.GENERAL_HEALTH -> Prescription(50, 50, 10..15, 60..60, "seimbang, variasi tinggi")
        GoalCategory.ENDURANCE -> Prescription(30, 70, 12..20, 30..45, "kardio progresif, beban pelengkap")
    }
}
