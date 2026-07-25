package com.fghilmany.nufitai.data.exerciselibrary.datasource

import com.fghilmany.nufitai.db.Exercise as ExerciseRow
import com.fghilmany.nufitai.db.NuFitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseLibraryLocalDataSource(private val database: NuFitDatabase) {

    suspend fun count(): Long = withContext(Dispatchers.Default) {
        database.exerciseQueries.countExercises().executeAsOne()
    }

    suspend fun insertAll(rows: List<ExerciseInsertRow>) = withContext(Dispatchers.Default) {
        database.transaction {
            rows.forEach { row ->
                database.exerciseQueries.insertExercise(
                    id = row.id,
                    name = row.name,
                    equipment_category = row.equipmentCategory,
                    movement_pattern = row.movementPattern,
                    level = row.level,
                    level_variant = row.levelVariant?.toLong(),
                    level_note = row.levelNote,
                    flag_exclusion = row.flagExclusionJson,
                    flag_prioritas = row.flagPrioritasJson,
                    area_terbebani = row.areaTerbebaniJson,
                    substitusi_setara = row.substitusiSetaraJson,
                    rantai_regresi = row.rantaiRegresi,
                    rantai_progresi = row.rantaiProgresi,
                    syarat_naik = row.syaratNaik,
                    pola_gerak_terkait = row.polaGerakTerkaitJson,
                    high_impact = if (row.highImpact) 1L else 0L,
                    isometric_heavy = if (row.isometricHeavy) 1L else 0L,
                    media_slug = row.mediaSlug,
                    primary_muscle_group = row.primaryMuscleGroup,
                    target_muscles_primary = row.targetMusclesPrimaryJson,
                    target_muscles_secondary = row.targetMusclesSecondaryJson,
                    instructions = row.instructionsJson,
                    common_mistakes = row.commonMistakesJson,
                    safety_tips = row.safetyTipsJson,
                )
            }
        }
    }

    suspend fun getAll(): List<ExerciseRow> = withContext(Dispatchers.Default) {
        database.exerciseQueries.getAllExercises().executeAsList()
    }

    suspend fun getById(id: String): ExerciseRow? = withContext(Dispatchers.Default) {
        database.exerciseQueries.getExerciseById(id).executeAsOneOrNull()
    }
}

/** Pre-encoded (JSON-string) shape ready for the `.sq` insert -- encoding happens in the repository. */
data class ExerciseInsertRow(
    val id: String,
    val name: String,
    val equipmentCategory: String,
    val movementPattern: String,
    val level: String,
    val levelVariant: Int?,
    val levelNote: String?,
    val flagExclusionJson: String,
    val flagPrioritasJson: String,
    val areaTerbebaniJson: String?,
    val substitusiSetaraJson: String?,
    val rantaiRegresi: String?,
    val rantaiProgresi: String?,
    val syaratNaik: String?,
    val polaGerakTerkaitJson: String?,
    val highImpact: Boolean,
    val isometricHeavy: Boolean,
    val mediaSlug: String?,
    val primaryMuscleGroup: String,
    val targetMusclesPrimaryJson: String,
    val targetMusclesSecondaryJson: String,
    val instructionsJson: String,
    val commonMistakesJson: String,
    val safetyTipsJson: String,
)
