package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.onboarding.entity.Level

enum class TestScoreCategory { KURANG, CUKUP, BAIK, SANGAT_BAIK }

/**
 * issue #29 layer 5 -- CAL-01..05. Pure, no I/O.
 *
 * KNOWN GAP: CAL-02/03/04 require age+gender-normed threshold tables from
 * `tahap-5-tes-kapasitas-fisik.md` to convert a raw pushup/plank/sit-to-stand score into
 * [TestScoreCategory] -- those norm tables were not available in any issue/doc read for this
 * implementation. [scoreCategory] must be supplied by the caller; until the norm tables are
 * wired up, callers should pass `null` (falls through to the CAL-05 safe default) rather than
 * fabricate thresholds. CAL-06/CAL-07 (norm-table gender/versi conversion) are not implemented
 * for the same reason -- there is nothing to convert without the base tables.
 */
object KalibrasiStartingLevel {
    operator fun invoke(
        hasExclusionFlagForPattern: Boolean,
        hasDirectTest: Boolean,
        scoreCategory: TestScoreCategory?,
        level: Level,
    ): ExerciseLevel {
        if (hasExclusionFlagForPattern) return ExerciseLevel.REGRESI // CAL-01: wins over any score

        if (!hasDirectTest) {
            // hinge/pull_horizontal/pull_vertical/carry: no Tahap 5 test maps here -> level_global_tahap_6
            return if (level == Level.BEGINNER) ExerciseLevel.REGRESI else ExerciseLevel.STANDAR
        }

        return when (scoreCategory) {
            null -> ExerciseLevel.REGRESI // CAL-05: skipped test (or norm table unavailable) = safest default
            TestScoreCategory.KURANG -> ExerciseLevel.REGRESI // CAL-02
            TestScoreCategory.CUKUP -> ExerciseLevel.STANDAR // CAL-03
            TestScoreCategory.BAIK, TestScoreCategory.SANGAT_BAIK -> // CAL-04
                if (level == Level.BEGINNER) ExerciseLevel.STANDAR else ExerciseLevel.PROGRESI
        }
    }
}
