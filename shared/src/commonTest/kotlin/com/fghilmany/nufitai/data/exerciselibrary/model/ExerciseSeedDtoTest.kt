package com.fghilmany.nufitai.data.exerciselibrary.model

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExerciseSeedDtoTest {

    @Test
    fun `given lowercase area and flag tokens when toEntity then normalizes correctly`() {
        val dto = ExerciseSeedDto(
            id = "0001",
            name = "Box Squat",
            equipmentCategory = "BODYWEIGHT",
            movementPattern = "SQUAT",
            level = "REGRESSION",
            flagExclusion = listOf("flag_ankle_mobility"),
            flagPriority = listOf("flag_knee_valgus"),
            loadedBodyAreas = listOf("lutut", "punggung_bawah"),
            primaryMuscleGroup = "LEG",
        )

        val entity = dto.toEntity()

        assertEquals(EquipmentCategory.BODYWEIGHT, entity.equipmentCategory)
        assertEquals(MovementPattern.SQUAT, entity.movementPattern)
        assertEquals(com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel.REGRESSION, entity.level)
        assertEquals(setOf(ExerciseFlag.MOVEMENT_ANKLE_MOBILITY), entity.flagExclusion)
        assertEquals(setOf(ExerciseFlag.MOVEMENT_KNEE_VALGUS), entity.flagPriority)
        assertEquals(setOf(BodyArea.KNEE, BodyArea.LOWER_BACK), entity.loadedBodyAreas)
    }

    @Test
    fun `given ADR-007 equivalentSubstitutes shape when toEntity then passes through unchanged`() {
        val dto = ExerciseSeedDto(
            id = "0002",
            name = "Farmer's Carry",
            equipmentCategory = "DUMBBELL",
            movementPattern = "CARRY",
            level = "STANDARD",
            primaryMuscleGroup = "CORE",
            equivalentSubstitutes = mapOf("duplicate_variant" to "0003", "bodyweight_alt" to "0004", "note" to "free text"),
        )

        val entity = dto.toEntity()

        assertEquals(mapOf("duplicate_variant" to "0003", "bodyweight_alt" to "0004", "note" to "free text"), entity.equivalentSubstitutes)
    }

    @Test
    fun `given instruction_steps with only an English key when toEntity then falls back to English`() {
        val dto = ExerciseSeedDto(
            id = "0005",
            name = "3-4 sit-up",
            equipmentCategory = "BODYWEIGHT",
            movementPattern = "CORE",
            level = "STANDARD",
            primaryMuscleGroup = "CORE",
            instructionSteps = mapOf("en" to listOf("Lie down.", "Sit up.")),
        )

        assertEquals(listOf("Lie down.", "Sit up."), dto.toEntity().instructions)
    }

    @Test
    fun `given instruction_steps with both id and en keys when toEntity then prefers Indonesian`() {
        val dto = ExerciseSeedDto(
            id = "0006",
            name = "3-4 sit-up",
            equipmentCategory = "BODYWEIGHT",
            movementPattern = "CORE",
            level = "STANDARD",
            primaryMuscleGroup = "CORE",
            instructionSteps = mapOf("en" to listOf("Lie down."), "id" to listOf("Berbaring.")),
        )

        assertEquals(listOf("Berbaring."), dto.toEntity().instructions)
    }

    @Test
    fun `given image and gif_url when toEntity then resolves to files-relative resource paths`() {
        val dto = ExerciseSeedDto(
            id = "0007",
            name = "3-4 sit-up",
            equipmentCategory = "BODYWEIGHT",
            movementPattern = "CORE",
            level = "STANDARD",
            primaryMuscleGroup = "CORE",
            image = "images/0007-abc.jpg",
            gifUrl = "videos/0007-abc.gif",
        )

        val entity = dto.toEntity()

        assertEquals("files/images/0007-abc.jpg", entity.imagePath)
        assertEquals("files/videos/0007-abc.gif", entity.gifPath)
    }

    @Test
    fun `given no image or gif_url when toEntity then media paths are null`() {
        val dto = ExerciseSeedDto(
            id = "0008",
            name = "3-4 sit-up",
            equipmentCategory = "BODYWEIGHT",
            movementPattern = "CORE",
            level = "STANDARD",
            primaryMuscleGroup = "CORE",
        )

        val entity = dto.toEntity()

        assertNull(entity.imagePath)
        assertNull(entity.gifPath)
    }
}
