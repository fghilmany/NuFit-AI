package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import kotlin.test.Test
import kotlin.test.assertEquals

class KalibrasiStartingLevelTest {

    @Test
    fun `given an active exclusion flag when invoked then CAL-01 wins regardless of score`() {
        val result = KalibrasiStartingLevel(
            hasExclusionFlagForPattern = true,
            hasDirectTest = true,
            scoreCategory = TestScoreCategory.SANGAT_BAIK,
            level = Level.INTERMEDIATE,
        )
        assertEquals(ExerciseLevel.REGRESI, result)
    }

    @Test
    fun `given a skipped test when invoked then CAL-05 defaults to Regresi`() {
        val result = KalibrasiStartingLevel(
            hasExclusionFlagForPattern = false,
            hasDirectTest = true,
            scoreCategory = null,
            level = Level.INTERMEDIATE,
        )
        assertEquals(ExerciseLevel.REGRESI, result)
    }

    @Test
    fun `given a pattern with no direct test when invoked then falls back to level-global default`() {
        val beginner = KalibrasiStartingLevel(false, hasDirectTest = false, scoreCategory = null, level = Level.BEGINNER)
        val intermediate = KalibrasiStartingLevel(false, hasDirectTest = false, scoreCategory = null, level = Level.INTERMEDIATE)

        assertEquals(ExerciseLevel.REGRESI, beginner)
        assertEquals(ExerciseLevel.STANDAR, intermediate)
    }

    @Test
    fun `given a good score for a beginner when invoked then CAL-04 caps at Standar, not Progresi`() {
        val result = KalibrasiStartingLevel(false, hasDirectTest = true, scoreCategory = TestScoreCategory.BAIK, level = Level.BEGINNER)
        assertEquals(ExerciseLevel.STANDAR, result)
    }
}
