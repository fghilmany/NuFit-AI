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
        injuryHistoryNote: String?,
        capacityTestJson: String?,
        goal: String,
        sessionsPerWeek: Long,
        hariPilihanJson: String,
        sessionDurationMinutes: Long,
        activityLevel: String?,
        flagDetails: List<Pair<String, String>>, // (kind, flagName)
        updatedAt: Long,
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
                injury_history_note = injuryHistoryNote,
                capacity_test = capacityTestJson,
                goal = goal,
                sessions_per_week = sessionsPerWeek,
                selected_weekdays = hariPilihanJson,
                session_duration_minutes = sessionDurationMinutes,
                activity_level = activityLevel,
                updated_at = updatedAt,
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

    /** techspec chat-ai.md §7 -- sync engine cursor query, mirrors MonthlyPlanLocalDataSource's shape. */
    suspend fun getChangedSince(updatedAfter: Long): List<FullAssessmentResultRow> = withContext(Dispatchers.Default) {
        database.fullAssessmentResultQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun getById(id: String): FullAssessmentResultRow? = withContext(Dispatchers.Default) {
        database.fullAssessmentResultQueries.selectById(id).executeAsOneOrNull()
    }

    suspend fun backfillUserId(userId: String) = withContext(Dispatchers.Default) {
        database.fullAssessmentResultQueries.backfillUserId(userId)
    }

    suspend fun upsertFromRemote(row: FullAssessmentResultRow) = withContext(Dispatchers.Default) {
        database.fullAssessmentResultQueries.upsertFromRemote(
            id = row.id,
            completed_at = row.completed_at,
            age = row.age,
            gender = row.gender,
            par_q_answers = row.par_q_answers,
            hard_stop_flagged = row.hard_stop_flagged,
            hard_stop_acknowledged_at = row.hard_stop_acknowledged_at,
            equipment_preference = row.equipment_preference,
            injury_history = row.injury_history,
            injury_history_note = row.injury_history_note,
            capacity_test = row.capacity_test,
            goal = row.goal,
            sessions_per_week = row.sessions_per_week,
            selected_weekdays = row.selected_weekdays,
            session_duration_minutes = row.session_duration_minutes,
            activity_level = row.activity_level,
            user_id = row.user_id,
            updated_at = row.updated_at,
        )
    }
}
