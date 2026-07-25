package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.testExercise
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetExerciseAlternativesTest {

    @Test
    fun `given a equivalentSubstitutes entry when invoked then includes the cross-equipment alternative`() = runTest {
        val target = testExercise(
            id = "DB-SQUAT-000",
            equipmentCategory = EquipmentCategory.DUMBBELL,
            movementPattern = MovementPattern.SQUAT,
            equivalentSubstitutes = mapOf(EquipmentCategory.BODYWEIGHT to "BW-SQUAT-000"),
        )
        val crossEquipmentAlt = testExercise(id = "BW-SQUAT-000", equipmentCategory = EquipmentCategory.BODYWEIGHT, movementPattern = MovementPattern.SQUAT)
        val repository = FakeExerciseLibraryRepository().apply { exercises = listOf(target, crossEquipmentAlt) }

        val result = GetExerciseAlternatives(repository)(target)

        assertIs<AppResult.Success<List<*>>>(result)
        assertTrue(result.data.any { it.id == "BW-SQUAT-000" })
    }

    @Test
    fun `given a same-pattern same-equipment sibling when invoked then includes it and excludes self`() = runTest {
        val target = testExercise(id = "BW-SQUAT-000", equipmentCategory = EquipmentCategory.BODYWEIGHT, movementPattern = MovementPattern.SQUAT)
        val sibling = testExercise(id = "BW-SQUAT-001", equipmentCategory = EquipmentCategory.BODYWEIGHT, movementPattern = MovementPattern.SQUAT)
        val repository = FakeExerciseLibraryRepository().apply { exercises = listOf(target, sibling) }

        val result = GetExerciseAlternatives(repository)(target)

        assertIs<AppResult.Success<List<*>>>(result)
        val ids = result.data.map { (it as com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise).id }
        assertTrue("BW-SQUAT-001" in ids)
        assertTrue("BW-SQUAT-000" !in ids)
    }

    @Test
    fun `given a CORRECTIVE-level sibling when invoked then it is excluded`() = runTest {
        val target = testExercise(id = "BW-SQUAT-000", equipmentCategory = EquipmentCategory.BODYWEIGHT, movementPattern = MovementPattern.SQUAT)
        val correctiveSibling = testExercise(
            id = "BW-SQUAT-CORR",
            equipmentCategory = EquipmentCategory.BODYWEIGHT,
            movementPattern = MovementPattern.SQUAT,
            level = ExerciseLevel.CORRECTIVE,
        )
        val repository = FakeExerciseLibraryRepository().apply { exercises = listOf(target, correctiveSibling) }

        val result = GetExerciseAlternatives(repository)(target)

        assertIs<AppResult.Success<List<*>>>(result)
        assertTrue(result.data.isEmpty())
    }
}
