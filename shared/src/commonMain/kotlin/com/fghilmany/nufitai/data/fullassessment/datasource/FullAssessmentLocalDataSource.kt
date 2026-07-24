package com.fghilmany.nufitai.data.fullassessment.datasource

import com.fghilmany.nufitai.db.Full_assessment_flag_detail as FullAssessmentFlagDetailRow
import com.fghilmany.nufitai.db.Full_assessment_result as FullAssessmentResultRow
import com.fghilmany.nufitai.db.NuFitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FullAssessmentLocalDataSource(private val database: NuFitDatabase) {

    suspend fun insertFullAssessmentResult(
        id: String,
        completedAt: Long,
        usia: Long?,
        gender: String?,
        parQAnswersJson: String,
        hardStopFlagged: Boolean,
        hardStopAcknowledgedAt: Long?,
        preferensiAlatJson: String,
        riwayatCederaJson: String,
        capacityTestJson: String?,
        goal: String,
        frekuensiPerMinggu: Long,
        hariPilihanJson: String,
        durasiSesiMenit: Long,
        flagDetails: List<Pair<String, String>>, // (kind, flagName)
    ) = withContext(Dispatchers.Default) {
        database.transaction {
            database.fullAssessmentResultQueries.insertFullAssessmentResult(
                id = id,
                completed_at = completedAt,
                usia = usia,
                gender = gender,
                par_q_answers = parQAnswersJson,
                hard_stop_flagged = if (hardStopFlagged) 1L else 0L,
                hard_stop_acknowledged_at = hardStopAcknowledgedAt,
                preferensi_alat = preferensiAlatJson,
                riwayat_cedera = riwayatCederaJson,
                capacity_test = capacityTestJson,
                goal = goal,
                frekuensi_per_minggu = frekuensiPerMinggu,
                hari_pilihan = hariPilihanJson,
                durasi_sesi_menit = durasiSesiMenit,
            )
            flagDetails.forEachIndexed { index, (kind, flagName) ->
                database.fullAssessmentFlagDetailQueries.insertFullAssessmentFlagDetail(
                    id = "$id-flag-$index",
                    full_assessment_result_id = id,
                    kind = kind,
                    flag_name = flagName,
                )
            }
        }
    }

    suspend fun getLatestFullAssessmentResult(): FullAssessmentResultRow? = withContext(Dispatchers.Default) {
        database.fullAssessmentResultQueries.getLatestFullAssessmentResult().executeAsOneOrNull()
    }

    suspend fun getFlagsForResult(resultId: String): List<FullAssessmentFlagDetailRow> = withContext(Dispatchers.Default) {
        database.fullAssessmentFlagDetailQueries.getFlagsForResult(resultId).executeAsList()
    }
}
