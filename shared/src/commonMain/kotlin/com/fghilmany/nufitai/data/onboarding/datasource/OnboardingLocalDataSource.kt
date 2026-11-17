package com.fghilmany.nufitai.data.onboarding.datasource

import com.fghilmany.nufitai.db.NuFitDatabase
import com.fghilmany.nufitai.db.Par_q_answer_detail as ParQAnswerDetailRow
import com.fghilmany.nufitai.db.Par_q_result as ParQResultRow
import com.fghilmany.nufitai.db.Quick_assessment_result as QuickAssessmentResultRow
import com.fghilmany.nufitai.db.Body_measurement as BodyMeasurementRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnboardingLocalDataSource(private val database: NuFitDatabase) {

    suspend fun insertParQResult(
        id: String,
        answeredAt: Long,
        requiresDoctorConsult: Boolean,
        consultAcknowledgedAt: Long?,
        answers: List<Pair<String, Boolean>>, // (questionId as name, answer)
        answersJson: String,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.transaction {
            database.parQResultQueries.insertParQResult(
                id = id,
                answered_at = answeredAt,
                requires_doctor_consult = if (requiresDoctorConsult) 1L else 0L,
                consult_acknowledged_at = consultAcknowledgedAt,
                answers_json = answersJson,
                updated_at = updatedAt,
            )
            answers.forEachIndexed { index, (questionId, answer) ->
                database.parQAnswerDetailQueries.insertParQAnswerDetail(
                    id = "$id-answer-$index",
                    par_q_result_id = id,
                    question_id = questionId,
                    answer = if (answer) 1L else 0L,
                )
            }
        }
    }

    suspend fun acknowledgeConsult(parQResultId: String, acknowledgedAt: Long) = withContext(Dispatchers.Default) {
        database.parQResultQueries.acknowledgeConsult(acknowledgedAt, parQResultId)
    }

    suspend fun getLatestParQResult(): ParQResultRow? = withContext(Dispatchers.Default) {
        database.parQResultQueries.getLatestParQResult().executeAsOneOrNull()
    }

    suspend fun getAnswersForResult(parQResultId: String): List<ParQAnswerDetailRow> = withContext(Dispatchers.Default) {
        database.parQAnswerDetailQueries.getAnswersForResult(parQResultId).executeAsList()
    }

    suspend fun getParQChangedSince(updatedAfter: Long): List<ParQResultRow> = withContext(Dispatchers.Default) {
        database.parQResultQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun backfillParQUserId(userId: String) = withContext(Dispatchers.Default) {
        database.parQResultQueries.backfillUserId(userId)
    }

    /** Sync pull -- batched in one transaction (not one commit per row). Also rebuilds
     * par_q_answer_detail from each pulled row's answers_json -- that child table has no sync
     * participant of its own (see ParQResult.sq). */
    suspend fun upsertParQResultsFromRemote(rows: List<Pair<ParQResultRow, List<Pair<String, Boolean>>>>) =
        withContext(Dispatchers.Default) {
            database.transaction {
                rows.forEach { (row, answers) ->
                    val existing = database.parQResultQueries.getParQResultById(row.id).executeAsOneOrNull()
                    if (existing == null || existing.updated_at < row.updated_at) {
                        database.parQResultQueries.upsertFromRemote(
                            id = row.id,
                            answered_at = row.answered_at,
                            requires_doctor_consult = row.requires_doctor_consult,
                            consult_acknowledged_at = row.consult_acknowledged_at,
                            answers_json = row.answers_json,
                            user_id = row.user_id,
                            updated_at = row.updated_at,
                        )
                        database.parQAnswerDetailQueries.deleteAnswersForResult(row.id)
                        answers.forEachIndexed { index, (questionId, answer) ->
                            database.parQAnswerDetailQueries.insertParQAnswerDetail(
                                id = "${row.id}-answer-$index",
                                par_q_result_id = row.id,
                                question_id = questionId,
                                answer = if (answer) 1L else 0L,
                            )
                        }
                    }
                }
            }
        }

    suspend fun insertQuickAssessmentResult(
        id: String,
        answeredAt: Long,
        experience: String,
        goal: String,
        equipment: String,
        frequency: String,
        splitPreference: String,
        resolvedSplit: String,
        level: String,
        templateId: String,
        splitEducationalNote: String?,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.quickAssessmentResultQueries.insertQuickAssessmentResult(
            id = id,
            answered_at = answeredAt,
            experience = experience,
            goal = goal,
            equipment = equipment,
            frequency = frequency,
            split_preference = splitPreference,
            resolved_split = resolvedSplit,
            level = level,
            template_id = templateId,
            split_educational_note = splitEducationalNote,
            updated_at = updatedAt,
        )
    }

    suspend fun getLatestQuickAssessmentResult(): QuickAssessmentResultRow? = withContext(Dispatchers.Default) {
        database.quickAssessmentResultQueries.getLatestQuickAssessmentResult().executeAsOneOrNull()
    }

    suspend fun getQuickAssessmentChangedSince(updatedAfter: Long): List<QuickAssessmentResultRow> = withContext(Dispatchers.Default) {
        database.quickAssessmentResultQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun backfillQuickAssessmentUserId(userId: String) = withContext(Dispatchers.Default) {
        database.quickAssessmentResultQueries.backfillUserId(userId)
    }

    /** Sync pull -- batched in one transaction (not one commit per row). */
    suspend fun upsertQuickAssessmentsFromRemote(rows: List<QuickAssessmentResultRow>) = withContext(Dispatchers.Default) {
        database.transaction {
            rows.forEach { row ->
                val existing = database.quickAssessmentResultQueries.selectById(row.id).executeAsOneOrNull()
                if (existing == null || existing.updated_at < row.updated_at) {
                    database.quickAssessmentResultQueries.upsertFromRemote(
                        id = row.id,
                        answered_at = row.answered_at,
                        experience = row.experience,
                        goal = row.goal,
                        equipment = row.equipment,
                        frequency = row.frequency,
                        split_preference = row.split_preference,
                        resolved_split = row.resolved_split,
                        level = row.level,
                        template_id = row.template_id,
                        split_educational_note = row.split_educational_note,
                        user_id = row.user_id,
                        updated_at = row.updated_at,
                    )
                }
            }
        }
    }

    suspend fun insertBodyMeasurement(
        id: String,
        recordedAt: Long,
        heightCm: Double?,
        weightKg: Double?,
        updatedAt: Long,
    ) = withContext(Dispatchers.Default) {
        database.bodyMeasurementQueries.insertBodyMeasurement(
            id = id,
            recorded_at = recordedAt,
            height_cm = heightCm,
            weight_kg = weightKg,
            updated_at = updatedAt,
        )
    }

    suspend fun getLatestBodyMeasurement(): BodyMeasurementRow? = withContext(Dispatchers.Default) {
        database.bodyMeasurementQueries.getLatestBodyMeasurement().executeAsOneOrNull()
    }

    suspend fun getAllBodyMeasurements(): List<BodyMeasurementRow> = withContext(Dispatchers.Default) {
        database.bodyMeasurementQueries.getAllBodyMeasurements().executeAsList()
    }

    suspend fun getBodyMeasurementChangedSince(updatedAfter: Long): List<BodyMeasurementRow> = withContext(Dispatchers.Default) {
        database.bodyMeasurementQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun backfillBodyMeasurementUserId(userId: String) = withContext(Dispatchers.Default) {
        database.bodyMeasurementQueries.backfillUserId(userId)
    }

    /** Sync pull -- batched in one transaction (not one commit per row). */
    suspend fun upsertBodyMeasurementsFromRemote(rows: List<BodyMeasurementRow>) = withContext(Dispatchers.Default) {
        database.transaction {
            rows.forEach { row ->
                val existing = database.bodyMeasurementQueries.selectById(row.id).executeAsOneOrNull()
                if (existing == null || existing.updated_at < row.updated_at) {
                    database.bodyMeasurementQueries.upsertFromRemote(
                        id = row.id,
                        recorded_at = row.recorded_at,
                        height_cm = row.height_cm,
                        weight_kg = row.weight_kg,
                        user_id = row.user_id,
                        updated_at = row.updated_at,
                    )
                }
            }
        }
    }
}
