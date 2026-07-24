package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetExercisePoolTest {

    private fun exercise(id: String) = Exercise(
        id = id,
        name = id,
        equipmentCategory = EquipmentCategory.BODYWEIGHT,
        movementPattern = MovementPattern.SQUAT,
        level = ExerciseLevel.STANDAR,
        levelVariant = null,
        levelNote = null,
        flagExclusion = emptySet<ExerciseFlag>(),
        flagPrioritas = emptySet<ExerciseFlag>(),
        areaTerbebani = emptySet<BodyArea>(),
        substitusiSetara = null,
        rantaiRegresi = null,
        rantaiProgresi = null,
        syaratNaik = null,
        polaGerakTerkait = null,
        highImpact = false,
        isometricHeavy = false,
        mediaSlug = null,
    )

    @Test
    fun `given empty table when invoked then seeds before reading`() = runTest {
        val repository = FakeExerciseLibraryRepository().apply { exercises = listOf(exercise("BW-SQUAT-000")) }
        val getExercisePool = GetExercisePool(repository)

        val result = getExercisePool()

        assertEquals(1, repository.ensureSeededCallCount)
        assertIs<AppResult.Success<List<Exercise>>>(result)
        assertEquals(listOf("BW-SQUAT-000"), result.data.map { it.id })
    }
}
