package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.fake.testExercise
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterExercisesTest {

    private val squat = testExercise(
        id = "BW-SQUAT-000",
        equipmentCategory = EquipmentCategory.BODYWEIGHT,
        movementPattern = MovementPattern.SQUAT,
        level = ExerciseLevel.REGRESSION,
        primaryMuscleGroup = MuscleGroup.LEG,
    ).copy(name = "Bodyweight Squat")

    private val pushup = testExercise(
        id = "BW-PUSHH-000",
        equipmentCategory = EquipmentCategory.BODYWEIGHT,
        movementPattern = MovementPattern.PUSH_HORIZONTAL,
        level = ExerciseLevel.STANDARD,
        primaryMuscleGroup = MuscleGroup.CHEST,
    ).copy(name = "Push-up")

    private val barbellSquat = testExercise(
        id = "BB-SQUAT-000",
        equipmentCategory = EquipmentCategory.BARBELL,
        movementPattern = MovementPattern.SQUAT,
        level = ExerciseLevel.STANDARD,
        primaryMuscleGroup = MuscleGroup.LEG,
    ).copy(name = "Barbell Back Squat")

    private val corrective = testExercise(
        id = "BW-CORRECTIVE-000",
        level = ExerciseLevel.CORRECTIVE,
        primaryMuscleGroup = MuscleGroup.BACK,
    ).copy(name = "Cat-Cow")

    private val pool = listOf(squat, pushup, barbellSquat, corrective)

    @Test
    fun `given no query or filters when invoked then corrective and accessory are still excluded`() {
        val result = FilterExercises()(pool, query = "", filter = ExerciseFilter())

        assertEquals(setOf(squat.id, pushup.id, barbellSquat.id), result.map { it.id }.toSet())
    }

    @Test
    fun `given a name query when invoked then matches case-insensitive substring`() {
        val result = FilterExercises()(pool, query = "squat", filter = ExerciseFilter())

        assertEquals(setOf(squat.id, barbellSquat.id), result.map { it.id }.toSet())
    }

    @Test
    fun `given muscle group and equipment filters together when invoked then AND across categories`() {
        val filter = ExerciseFilter(muscleGroups = setOf(MuscleGroup.LEG), equipment = setOf(EquipmentCategory.BODYWEIGHT))

        val result = FilterExercises()(pool, query = "", filter = filter)

        assertEquals(listOf(squat.id), result.map { it.id })
    }

    @Test
    fun `given multiple values within one facet when invoked then OR within the facet`() {
        val filter = ExerciseFilter(equipment = setOf(EquipmentCategory.BODYWEIGHT, EquipmentCategory.BARBELL))

        val result = FilterExercises()(pool, query = "", filter = filter)

        assertEquals(setOf(squat.id, pushup.id, barbellSquat.id), result.map { it.id }.toSet())
    }
}
