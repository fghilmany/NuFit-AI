package com.fghilmany.nufitai.domain.fullassessment.entity

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.time.Instant

/**
 * Minimal Full Assessment stub (issue #76 scope) -- exactly the fields the tahap-7b rule
 * engine's `input_schema` (issue #29) needs, not the full P-11 experience `06-ai-custom-plan.md`
 * will expand later.
 *
 * [flagsPostural]/[flagsGerak] are restricted in practice to [ExerciseFlag]'s POSTURAL_ and
 * MOVEMENT_ subsets (issue #29 layer 3 taxonomy) -- kept as plain `Set<ExerciseFlag>` rather
 * than two separate parallel enums, since [ExerciseFlag] already carries that exact taxonomy.
 * [parQKategoriB] is restricted to the HEALTH_ subset, computed via
 * `FullAssessmentParQQuestionId.toExerciseFlagOrNull()`.
 */
data class FullAssessmentResult(
    val id: String,
    val completedAt: Instant,
    val usia: Int?,
    val gender: Gender?,
    val parQAnswers: List<FullAssessmentParQAnswer>,
    val parQKategoriB: Set<ExerciseFlag>,
    val hardStopFlagged: Boolean,
    val hardStopAcknowledgedAt: Instant?,
    val preferensiAlat: Set<EquipmentCategory>,
    val riwayatCedera: Set<BodyArea>,
    val flagsPostural: Set<ExerciseFlag>,
    val flagsGerak: Set<ExerciseFlag>,
    val capacityTest: CapacityTestResult?,
    /** issue #29 layer_4 (RESEP) input -- Quick Assessment's `GoalCategory` reused, `ENDURANCE` only reachable here. */
    val goal: GoalCategory,
    /** issue #29 layer_6 (JADWAL) input, Tahap 1 data. */
    val frekuensiPerMinggu: Int,
    /** ISO day-of-week, 1=Monday..7=Sunday. */
    val hariPilihan: Set<Int>,
    val durasiSesiMenit: Int,
)
