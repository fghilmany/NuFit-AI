package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.fake.testExercise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SafetyFilterTest {

    @Test
    fun `given an exercise excluded by an active flag when invoked then it is removed from the pool`() {
        val excluded = testExercise("BB-HINGE-000", flagExclusion = setOf(ExerciseFlag.MOVEMENT_HINGE_FROM_BACK))
        val safe = testExercise("BW-HINGE-000", movementPattern = MovementPattern.HINGE)
        val pool = mapOf(MovementPattern.HINGE to listOf(excluded, safe))

        val result = SafetyFilter(pool, activeFlags = setOf(ExerciseFlag.MOVEMENT_HINGE_FROM_BACK), painAreas = emptySet())

        assertEquals(listOf("BW-HINGE-000"), result.filteredPool[MovementPattern.HINGE]?.map { it.id })
    }

    @Test
    fun `given an exercise loading an area of reported pain when invoked then it is soft-blocked (SAFE-10)`() {
        val painful = testExercise("BB-SQUAT-000", movementPattern = MovementPattern.SQUAT, loadedBodyAreas = setOf(BodyArea.KNEE))
        val pool = mapOf(MovementPattern.SQUAT to listOf(painful))

        val result = SafetyFilter(pool, activeFlags = emptySet(), painAreas = setOf(BodyArea.KNEE))

        assertTrue(result.filteredPool[MovementPattern.SQUAT].orEmpty().isEmpty())
    }

    // SAFE-09, DoD: 3+ active flags -> corrective budget capped at 3, overflow surfaced (not dropped).
    @Test
    fun `given more than 3 flag-matching correctives when invoked then caps at 3 and returns the rest as overflow`() {
        val correctives = (1..5).map { index ->
            testExercise(
                "BW-CORR-00$index",
                movementPattern = MovementPattern.CORRECTIVE,
                flagPriority = setOf(ExerciseFlag.POSTURAL_FORWARD_HEAD),
            )
        }
        val pool = mapOf(MovementPattern.CORRECTIVE to correctives)

        val result = SafetyFilter(pool, activeFlags = setOf(ExerciseFlag.POSTURAL_FORWARD_HEAD), painAreas = emptySet())

        assertEquals(SAFE_09_BUDGET_MAX, result.mandatoryCorrective.size)
        assertEquals(2, result.correctiveOverflow.size)
        // Nothing is silently dropped -- every candidate is accounted for in one of the two lists.
        assertEquals(correctives.size, result.mandatoryCorrective.size + result.correctiveOverflow.size)
    }
}
