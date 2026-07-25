package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.onboarding.entity.Level

enum class TestScoreCategory { POOR, FAIR, GOOD, EXCELLENT }

/**
 * issue #29 layer 5 -- CAL-01..05. Pure, no I/O.
 *
 * KNOWN GAP: CAL-02/03/04 require age+gender-normed threshold tables from
 * `tahap-5-tes-kapasitas-fisik.md` to convert a raw pushup/plank/sit-to-stand score into
 * [TestScoreCategory] -- those norm tables were not available in any issue/doc read for this
 * implementation. [scoreCategory] must be supplied by the caller; until the norm tables are
 * wired up, callers should pass `null` (falls through to the CAL-05 safe default) rather than
 * fabricate thresholds. CAL-06/CAL-07 (norm-table gender/version conversion) are not implemented
 * for the same reason -- there is nothing to convert without the base tables.
 */
object CalibrateStartingLevel {
    operator fun invoke(
        hasExclusionFlagForPattern: Boolean,
        hasDirectTest: Boolean,
        scoreCategory: TestScoreCategory?,
        level: Level,
    ): ExerciseLevel {
        if (hasExclusionFlagForPattern) return ExerciseLevel.REGRESSION // CAL-01: wins over any score

        if (!hasDirectTest) {
            // hinge/pull_horizontal/pull_vertical/carry: no Tahap 5 test maps here -> level_global_tahap_6
            return if (level == Level.BEGINNER) ExerciseLevel.REGRESSION else ExerciseLevel.STANDARD
        }

        return when (scoreCategory) {
            null -> ExerciseLevel.REGRESSION // CAL-05: skipped test (or norm table unavailable) = safest default
            TestScoreCategory.POOR -> ExerciseLevel.REGRESSION // CAL-02
            TestScoreCategory.FAIR -> ExerciseLevel.STANDARD // CAL-03
            TestScoreCategory.GOOD, TestScoreCategory.EXCELLENT -> // CAL-04
                if (level == Level.BEGINNER) ExerciseLevel.STANDARD else ExerciseLevel.PROGRESSION
        }
    }
}
