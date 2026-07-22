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
    ) = withContext(Dispatchers.Default) {
        database.transaction {
            database.parQResultQueries.insertParQResult(
                id = id,
                answered_at = answeredAt,
                requires_doctor_consult = if (requiresDoctorConsult) 1L else 0L,
                consult_acknowledged_at = consultAcknowledgedAt,
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
        )
    }

    suspend fun getLatestQuickAssessmentResult(): QuickAssessmentResultRow? = withContext(Dispatchers.Default) {
        database.quickAssessmentResultQueries.getLatestQuickAssessmentResult().executeAsOneOrNull()
    }

    suspend fun insertBodyMeasurement(
        id: String,
        recordedAt: Long,
        heightCm: Double?,
        weightKg: Double?,
    ) = withContext(Dispatchers.Default) {
        database.bodyMeasurementQueries.insertBodyMeasurement(
            id = id,
            recorded_at = recordedAt,
            height_cm = heightCm,
            weight_kg = weightKg,
        )
    }

    suspend fun getLatestBodyMeasurement(): BodyMeasurementRow? = withContext(Dispatchers.Default) {
        database.bodyMeasurementQueries.getLatestBodyMeasurement().executeAsOneOrNull()
    }
}
