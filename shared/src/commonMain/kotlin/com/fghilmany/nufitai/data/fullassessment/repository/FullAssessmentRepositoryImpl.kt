package com.fghilmany.nufitai.data.fullassessment.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.fullassessment.datasource.FullAssessmentLocalDataSource
import com.fghilmany.nufitai.data.fullassessment.model.CapacityTestJson
import com.fghilmany.nufitai.data.fullassessment.model.ParQAnswerJson
import com.fghilmany.nufitai.db.Full_assessment_result as FullAssessmentResultRow
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.fullassessment.repository.FullAssessmentRepository
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Instant

private const val KIND_HEALTH = "HEALTH"
private const val KIND_POSTURAL = "POSTURAL"
private const val KIND_MOVEMENT = "MOVEMENT"

class FullAssessmentRepositoryImpl(
    private val local: FullAssessmentLocalDataSource,
) : FullAssessmentRepository {

    private val json = Json

    override suspend fun saveFullAssessmentResult(result: FullAssessmentResult): AppResult<Unit> = runCatchingDatabase {
        val flagDetails = buildList {
            result.parQKategoriB.forEach { add(KIND_HEALTH to it.name) }
            result.flagsPostural.forEach { add(KIND_POSTURAL to it.name) }
            result.flagsGerak.forEach { add(KIND_MOVEMENT to it.name) }
        }
        local.insertFullAssessmentResult(
            id = result.id,
            completedAt = result.completedAt.toEpochMilliseconds(),
            usia = result.usia?.toLong(),
            gender = result.gender?.name,
            parQAnswersJson = json.encodeToString(result.parQAnswers.map { ParQAnswerJson.from(it) }),
            hardStopFlagged = result.hardStopFlagged,
            hardStopAcknowledgedAt = result.hardStopAcknowledgedAt?.toEpochMilliseconds(),
            preferensiAlatJson = json.encodeToString(result.preferensiAlat),
            riwayatCederaJson = json.encodeToString(result.riwayatCedera),
            capacityTestJson = result.capacityTest?.let { json.encodeToString(CapacityTestJson.from(it)) },
            goal = result.goal.name,
            frekuensiPerMinggu = result.frekuensiPerMinggu.toLong(),
            hariPilihanJson = json.encodeToString(result.hariPilihan),
            durasiSesiMenit = result.durasiSesiMenit.toLong(),
            flagDetails = flagDetails,
        )
    }

    override suspend fun getLatestFullAssessmentResult(): AppResult<FullAssessmentResult?> = runCatchingDatabase {
        val row = local.getLatestFullAssessmentResult() ?: return@runCatchingDatabase null
        val flagRows = local.getFlagsForResult(row.id)
        row.toEntity(
            parQKategoriB = flagRows.filter { it.kind == KIND_HEALTH }.map { ExerciseFlag.valueOf(it.flag_name) }.toSet(),
            flagsPostural = flagRows.filter { it.kind == KIND_POSTURAL }.map { ExerciseFlag.valueOf(it.flag_name) }.toSet(),
            flagsGerak = flagRows.filter { it.kind == KIND_MOVEMENT }.map { ExerciseFlag.valueOf(it.flag_name) }.toSet(),
        )
    }

    override suspend fun hasCompletedFullAssessment(): AppResult<Boolean> = runCatchingDatabase {
        local.getLatestFullAssessmentResult() != null
    }

    private fun FullAssessmentResultRow.toEntity(
        parQKategoriB: Set<ExerciseFlag>,
        flagsPostural: Set<ExerciseFlag>,
        flagsGerak: Set<ExerciseFlag>,
    ): FullAssessmentResult = FullAssessmentResult(
        id = id,
        completedAt = Instant.fromEpochMilliseconds(completed_at),
        usia = usia?.toInt(),
        gender = gender?.let { Gender.valueOf(it) },
        parQAnswers = json.decodeFromString<List<ParQAnswerJson>>(par_q_answers).map { it.toEntity() },
        parQKategoriB = parQKategoriB,
        hardStopFlagged = hard_stop_flagged == 1L,
        hardStopAcknowledgedAt = hard_stop_acknowledged_at?.let { Instant.fromEpochMilliseconds(it) },
        preferensiAlat = json.decodeFromString<Set<EquipmentCategory>>(preferensi_alat),
        riwayatCedera = json.decodeFromString<Set<BodyArea>>(riwayat_cedera),
        flagsPostural = flagsPostural,
        flagsGerak = flagsGerak,
        capacityTest = capacity_test?.let { json.decodeFromString<CapacityTestJson>(it).toEntity() },
        goal = GoalCategory.valueOf(goal),
        frekuensiPerMinggu = frekuensi_per_minggu.toInt(),
        hariPilihan = json.decodeFromString<Set<Int>>(hari_pilihan),
        durasiSesiMenit = durasi_sesi_menit.toInt(),
    )
}
