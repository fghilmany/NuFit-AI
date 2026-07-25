package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import kotlin.test.Test
import kotlin.test.assertEquals

class CalibrateStartingLevelTest {

    @Test
    fun `given an active exclusion flag when invoked then CAL-01 wins regardless of score`() {
        val result = CalibrateStartingLevel(
            hasExclusionFlagForPattern = true,
            hasDirectTest = true,
            scoreCategory = TestScoreCategory.EXCELLENT,
            level = Level.INTERMEDIATE,
        )
        assertEquals(ExerciseLevel.REGRESSION, result)
    }

    @Test
    fun `given a skipped test when invoked then CAL-05 defaults to Regression`() {
        val result = CalibrateStartingLevel(
            hasExclusionFlagForPattern = false,
            hasDirectTest = true,
            scoreCategory = null,
            level = Level.INTERMEDIATE,
        )
        assertEquals(ExerciseLevel.REGRESSION, result)
    }

    @Test
    fun `given a pattern with no direct test when invoked then falls back to level-global default`() {
        val beginner = CalibrateStartingLevel(false, hasDirectTest = false, scoreCategory = null, level = Level.BEGINNER)
        val intermediate = CalibrateStartingLevel(false, hasDirectTest = false, scoreCategory = null, level = Level.INTERMEDIATE)

        assertEquals(ExerciseLevel.REGRESSION, beginner)
        assertEquals(ExerciseLevel.STANDARD, intermediate)
    }

    @Test
    fun `given a good score for a beginner when invoked then CAL-04 caps at Standard, not Progression`() {
        val result = CalibrateStartingLevel(false, hasDirectTest = true, scoreCategory = TestScoreCategory.GOOD, level = Level.BEGINNER)
        assertEquals(ExerciseLevel.STANDARD, result)
    }
}
