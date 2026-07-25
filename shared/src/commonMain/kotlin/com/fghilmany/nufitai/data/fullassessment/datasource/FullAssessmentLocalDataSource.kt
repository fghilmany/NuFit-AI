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
        age: Long?,
        gender: String?,
        parQAnswersJson: String,
        hardStopFlagged: Boolean,
        hardStopAcknowledgedAt: Long?,
        equipmentPreferenceJson: String,
        injuryHistoryJson: String,
        capacityTestJson: String?,
        goal: String,
        sessionsPerWeek: Long,
        hariPilihanJson: String,
        sessionDurationMinutes: Long,
        flagDetails: List<Pair<String, String>>, // (kind, flagName)
    ) = withContext(Dispatchers.Default) {
        database.transaction {
            database.fullAssessmentResultQueries.insertFullAssessmentResult(
                id = id,
                completed_at = completedAt,
                age = age,
                gender = gender,
                par_q_answers = parQAnswersJson,
                hard_stop_flagged = if (hardStopFlagged) 1L else 0L,
                hard_stop_acknowledged_at = hardStopAcknowledgedAt,
                equipment_preference = equipmentPreferenceJson,
                injury_history = injuryHistoryJson,
                capacity_test = capacityTestJson,
                goal = goal,
                sessions_per_week = sessionsPerWeek,
                selected_weekdays = hariPilihanJson,
                session_duration_minutes = sessionDurationMinutes,
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
