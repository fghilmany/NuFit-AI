package com.fghilmany.nufitai.data.exerciselibrary.model

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExerciseSeedDtoTest {

    @Test
    fun `given lowercase area and flag tokens when toEntity then normalizes correctly`() {
        val dto = ExerciseSeedDto(
            id = "BW-SQUAT-000",
            name = "Box Squat",
            equipmentCategory = "BODYWEIGHT",
            movementPattern = "SQUAT",
            level = "REGRESI",
            flagExclusion = listOf("flag_ankle_mobility"),
            flagPrioritas = listOf("flag_knee_valgus"),
            areaTerbebani = listOf("lutut", "punggung_bawah"),
        )

        val entity = dto.toEntity()

        assertEquals(EquipmentCategory.BODYWEIGHT, entity.equipmentCategory)
        assertEquals(MovementPattern.SQUAT, entity.movementPattern)
        assertEquals(ExerciseLevel.REGRESI, entity.level)
        assertEquals(setOf(ExerciseFlag.MOVEMENT_ANKLE_MOBILITY), entity.flagExclusion)
        assertEquals(setOf(ExerciseFlag.MOVEMENT_KNEE_VALGUS), entity.flagPrioritas)
        assertEquals(setOf(BodyArea.LUTUT, BodyArea.PUNGGUNG_BAWAH), entity.areaTerbebani)
    }

    @Test
    fun `given substitusiSetara with an unresolvable band key when toEntity then drops it without crashing`() {
        val dto = ExerciseSeedDto(
            id = "DB-CARRY-000",
            name = "Farmer's Carry",
            equipmentCategory = "DUMBBELL",
            movementPattern = "CARRY",
            level = "STANDAR",
            substitusiSetara = mapOf("band" to "BAND-CARRY-000", "mesin" to "MC-CARRY-000"),
        )

        val entity = dto.toEntity()

        assertEquals(mapOf(EquipmentCategory.MACHINE_CABLE to "MC-CARRY-000"), entity.substitusiSetara)
    }

    @Test
    fun `given only unresolvable substitusiSetara keys when toEntity then result is null`() {
        val dto = ExerciseSeedDto(
            id = "DB-CARRY-000",
            name = "Farmer's Carry",
            equipmentCategory = "DUMBBELL",
            movementPattern = "CARRY",
            level = "STANDAR",
            substitusiSetara = mapOf("band" to "BAND-CARRY-000"),
        )

        assertNull(dto.toEntity().substitusiSetara)
    }
}
