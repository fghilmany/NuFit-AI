package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory

data class Resep(
    val rasioBebanPersen: Int,
    val rasioKardioPersen: Int,
    val repRange: IntRange,
    val istirahatDetik: IntRange,
    val struktur: String,
)

/** issue #29 layer 4 -- pure mapping table, no I/O. */
object ResepGoal {
    operator fun invoke(goal: GoalCategory): Resep = when (goal) {
        GoalCategory.FAT_LOSS -> Resep(60, 40, 10..15, 45..60, "compound full-body, circuit ringan")
        GoalCategory.MUSCLE_GAIN -> Resep(80, 20, 8..12, 60..90, "volume lebih tinggi per grup otot")
        GoalCategory.STRENGTH -> Resep(90, 10, 6..10, 90..120, "beban fokus, rep rendah")
        GoalCategory.GENERAL_HEALTH -> Resep(50, 50, 10..15, 60..60, "seimbang, variasi tinggi")
        GoalCategory.ENDURANCE -> Resep(30, 70, 12..20, 30..45, "kardio progresif, beban pelengkap")
    }
}
