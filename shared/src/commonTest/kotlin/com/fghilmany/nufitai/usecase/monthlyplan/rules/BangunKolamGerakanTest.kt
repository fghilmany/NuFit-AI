package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.fake.testExercise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BangunKolamGerakanTest {

    @Test
    fun `given bodyweight-only selection when invoked then dumbbell exercises are excluded but bodyweight always included`() {
        val exercises = listOf(
            testExercise("BW-SQUAT-000", equipmentCategory = EquipmentCategory.BODYWEIGHT),
            testExercise("DB-SQUAT-000", equipmentCategory = EquipmentCategory.DUMBBELL),
        )

        val pool = BangunKolamGerakan(exercises, preferensiAlat = emptySet(), level = Level.BEGINNER)

        assertEquals(listOf("BW-SQUAT-000"), pool[MovementPattern.SQUAT]?.map { it.id })
    }

    @Test
    fun `given beginner level when invoked then barbell is deferred out of the pool entirely`() {
        val exercises = listOf(
            testExercise("BB-SQUAT-000", equipmentCategory = EquipmentCategory.BARBELL),
            testExercise("BW-SQUAT-000", equipmentCategory = EquipmentCategory.BODYWEIGHT),
        )

        val pool = BangunKolamGerakan(exercises, preferensiAlat = setOf(EquipmentCategory.BARBELL), level = Level.BEGINNER)

        assertEquals(listOf("BW-SQUAT-000"), pool[MovementPattern.SQUAT]?.map { it.id })
    }

    @Test
    fun `given intermediate level when invoked then barbell is included`() {
        val exercises = listOf(testExercise("BB-SQUAT-000", equipmentCategory = EquipmentCategory.BARBELL))

        val pool = BangunKolamGerakan(exercises, preferensiAlat = setOf(EquipmentCategory.BARBELL), level = Level.INTERMEDIATE)

        assertTrue(pool[MovementPattern.SQUAT]?.map { it.id }?.contains("BB-SQUAT-000") == true)
    }

    @Test
    fun `given mixed levels when invoked then each pattern group is sorted regresi before standar before progresi`() {
        val exercises = listOf(
            testExercise("BW-SQUAT-PROG", level = ExerciseLevel.PROGRESI),
            testExercise("BW-SQUAT-REG", level = ExerciseLevel.REGRESI),
            testExercise("BW-SQUAT-STD", level = ExerciseLevel.STANDAR),
        )

        val pool = BangunKolamGerakan(exercises, preferensiAlat = emptySet(), level = Level.BEGINNER)

        assertEquals(listOf("BW-SQUAT-REG", "BW-SQUAT-STD", "BW-SQUAT-PROG"), pool[MovementPattern.SQUAT]?.map { it.id })
    }

    // POOL-04, DoD: sparse equipment selection (Bodyweight-only) with an empty pattern -> fallback via substitusiSetara.
    @Test
    fun `given bodyweight-only pool with no Pull Vertical exercises when fallbackFor invoked then finds a substitusiSetara candidate`() {
        val pullUpBarExercise = testExercise("PUB-PULLV-000", equipmentCategory = EquipmentCategory.PULL_UP_BAR, movementPattern = MovementPattern.PULL_VERTICAL)
        val bodyweightWithSubstitute = testExercise(
            "BW-PULLH-000",
            equipmentCategory = EquipmentCategory.BODYWEIGHT,
            movementPattern = MovementPattern.PULL_VERTICAL, // hypothetical: only entry in this pattern is the substitute target itself
            substitusiSetara = mapOf(EquipmentCategory.PULL_UP_BAR to "PUB-PULLV-000"),
        )
        val allExercises = listOf(pullUpBarExercise, bodyweightWithSubstitute)

        val fallback = BangunKolamGerakan.fallbackFor(MovementPattern.PULL_VERTICAL, allExercises, excludedIds = setOf("PUB-PULLV-000"))

        assertEquals("PUB-PULLV-000", fallback?.id)
    }

    @Test
    fun `given a pattern with truly no exercises anywhere when fallbackFor invoked then returns null`() {
        val fallback = BangunKolamGerakan.fallbackFor(MovementPattern.PULL_VERTICAL, allExercises = emptyList(), excludedIds = emptySet())

        assertNull(fallback)
    }
}
