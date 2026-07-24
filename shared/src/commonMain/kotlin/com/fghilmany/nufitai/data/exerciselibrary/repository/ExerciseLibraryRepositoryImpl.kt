package com.fghilmany.nufitai.data.exerciselibrary.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.exerciselibrary.datasource.ExerciseInsertRow
import com.fghilmany.nufitai.data.exerciselibrary.datasource.ExerciseLibraryLocalDataSource
import com.fghilmany.nufitai.data.exerciselibrary.datasource.ExerciseLibrarySeedDataSource
import com.fghilmany.nufitai.db.Exercise as ExerciseRow
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExerciseLibraryRepositoryImpl(
    private val local: ExerciseLibraryLocalDataSource,
    private val seed: ExerciseLibrarySeedDataSource,
) : ExerciseLibraryRepository {

    private val json = Json

    override suspend fun ensureSeeded(): AppResult<Unit> = runCatchingDatabase {
        if (local.count() > 0) return@runCatchingDatabase
        // Route through Exercise (not the raw DTO) so insert/read share one normalization
        // pass -- DTO fields are raw source tokens ("flag_ankle_mobility", "mesin"), not the
        // ExerciseFlag/EquipmentCategory enum names the JSON columns store on read-back.
        val rows = seed.readSeed().map { it.toEntity().toInsertRow() }
        local.insertAll(rows)
    }

    override suspend fun getAll(): AppResult<List<Exercise>> = runCatchingDatabase {
        local.getAll().map { it.toEntity() }
    }

    override suspend fun getById(id: String): AppResult<Exercise?> = runCatchingDatabase {
        local.getById(id)?.toEntity()
    }

    private fun Exercise.toInsertRow(): ExerciseInsertRow = ExerciseInsertRow(
        id = id,
        name = name,
        equipmentCategory = equipmentCategory.name,
        movementPattern = movementPattern.name,
        level = level.name,
        levelVariant = levelVariant,
        levelNote = levelNote,
        flagExclusionJson = json.encodeToString(flagExclusion),
        flagPrioritasJson = json.encodeToString(flagPrioritas),
        areaTerbebaniJson = areaTerbebani?.let { json.encodeToString(it) },
        substitusiSetaraJson = substitusiSetara?.let { json.encodeToString(it) },
        rantaiRegresi = rantaiRegresi,
        rantaiProgresi = rantaiProgresi,
        syaratNaik = syaratNaik,
        polaGerakTerkaitJson = polaGerakTerkait?.let { json.encodeToString(it) },
        highImpact = highImpact,
        isometricHeavy = isometricHeavy,
        mediaSlug = mediaSlug,
    )

    private fun ExerciseRow.toEntity(): Exercise = Exercise(
        id = id,
        name = name,
        equipmentCategory = EquipmentCategory.valueOf(equipment_category),
        movementPattern = MovementPattern.valueOf(movement_pattern),
        level = ExerciseLevel.valueOf(level),
        levelVariant = level_variant?.toInt(),
        levelNote = level_note,
        flagExclusion = json.decodeFromString<Set<ExerciseFlag>>(flag_exclusion),
        flagPrioritas = json.decodeFromString<Set<ExerciseFlag>>(flag_prioritas),
        areaTerbebani = area_terbebani?.let { json.decodeFromString<Set<BodyArea>>(it) },
        substitusiSetara = substitusi_setara?.let { json.decodeFromString<Map<EquipmentCategory, String>>(it) },
        rantaiRegresi = rantai_regresi,
        rantaiProgresi = rantai_progresi,
        syaratNaik = syarat_naik,
        polaGerakTerkait = pola_gerak_terkait?.let { json.decodeFromString<Set<MovementPattern>>(it) },
        highImpact = high_impact == 1L,
        isometricHeavy = isometric_heavy == 1L,
        mediaSlug = media_slug,
    )
}
