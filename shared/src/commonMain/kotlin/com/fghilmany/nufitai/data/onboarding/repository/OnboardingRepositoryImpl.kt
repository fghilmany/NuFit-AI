package com.fghilmany.nufitai.data.onboarding.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.onboarding.datasource.OnboardingLocalDataSource
import com.fghilmany.nufitai.db.Body_measurement as BodyMeasurementRow
import com.fghilmany.nufitai.db.Par_q_result as ParQResultRow
import com.fghilmany.nufitai.db.Quick_assessment_result as QuickAssessmentResultRow
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.entity.EquipmentType
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ParQAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.domain.onboarding.entity.ParQResult
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentResult
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference
import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.toHealthFlags
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository
import kotlin.time.Instant

class OnboardingRepositoryImpl(
    private val local: OnboardingLocalDataSource,
) : OnboardingRepository {

    override suspend fun saveParQResult(result: ParQResult): AppResult<Unit> = runCatchingDatabase {
        local.insertParQResult(
            id = result.id,
            answeredAt = result.answeredAt.toEpochMilliseconds(),
            requiresDoctorConsult = result.requiresDoctorConsult,
            consultAcknowledgedAt = result.consultAcknowledgedAt?.toEpochMilliseconds(),
            answers = result.answers.map { it.questionId.name to it.answer },
        )
    }

    override suspend fun acknowledgeDoctorConsult(parQResultId: String, acknowledgedAt: Instant): AppResult<Unit> =
        runCatchingDatabase {
            local.acknowledgeConsult(parQResultId, acknowledgedAt.toEpochMilliseconds())
        }

    override suspend fun getLatestParQResult(): AppResult<ParQResult?> = runCatchingDatabase {
        val row = local.getLatestParQResult() ?: return@runCatchingDatabase null
        val answerRows = local.getAnswersForResult(row.id)
        row.toEntity(answerRows.map { ParQAnswer(ParQQuestionId.valueOf(it.question_id), it.answer == 1L) })
    }

    override suspend fun saveQuickAssessmentResult(result: QuickAssessmentResult): AppResult<Unit> =
        runCatchingDatabase {
            local.insertQuickAssessmentResult(
                id = result.id,
                answeredAt = result.answeredAt.toEpochMilliseconds(),
                experience = result.input.experience.name,
                goal = result.input.goal.name,
                equipment = result.input.equipment.joinToString(",") { it.name },
                frequency = result.input.frequency.name,
                splitPreference = result.input.splitPreference.name,
                resolvedSplit = result.resolvedSplit.name,
                level = result.level.name,
                templateId = result.templateId,
                splitEducationalNote = result.splitEducationalNote,
            )
        }

    override suspend fun getLatestQuickAssessmentResult(): AppResult<QuickAssessmentResult?> = runCatchingDatabase {
        local.getLatestQuickAssessmentResult()?.toEntity()
    }

    override suspend fun saveBodyMeasurement(measurement: BodyMeasurement): AppResult<Unit> = runCatchingDatabase {
        local.insertBodyMeasurement(
            id = measurement.id,
            recordedAt = measurement.recordedAt.toEpochMilliseconds(),
            heightCm = measurement.heightCm,
            weightKg = measurement.weightKg,
        )
    }

    override suspend fun getLatestBodyMeasurement(): AppResult<BodyMeasurement?> = runCatchingDatabase {
        local.getLatestBodyMeasurement()?.toEntity()
    }

    override suspend fun hasLocalProfile(): AppResult<Boolean> = runCatchingDatabase {
        local.getLatestQuickAssessmentResult() != null
    }
}

private fun ParQResultRow.toEntity(answers: List<ParQAnswer>): ParQResult {
    val flags = answers.filter { it.answer }
        .flatMap { it.questionId.toHealthFlags() }
        .toSet()
    return ParQResult(
        id = id,
        answeredAt = Instant.fromEpochMilliseconds(answered_at),
        answers = answers,
        flagsGenerated = flags,
        requiresDoctorConsult = requires_doctor_consult == 1L,
        consultAcknowledgedAt = consult_acknowledged_at?.let { Instant.fromEpochMilliseconds(it) },
    )
}

private fun QuickAssessmentResultRow.toEntity(): QuickAssessmentResult = QuickAssessmentResult(
    id = id,
    answeredAt = Instant.fromEpochMilliseconds(answered_at),
    input = QuickAssessmentAnswer(
        experience = Experience.valueOf(experience),
        goal = GoalCategory.valueOf(goal),
        equipment = equipment.split(",").filter { it.isNotBlank() }.map { EquipmentType.valueOf(it) }.toSet(),
        frequency = FrequencyBucket.valueOf(frequency),
        splitPreference = SplitPreference.valueOf(split_preference),
    ),
    level = Level.valueOf(level),
    resolvedSplit = ResolvedSplit.valueOf(resolved_split),
    splitEducationalNote = split_educational_note,
    templateId = template_id,
)

private fun BodyMeasurementRow.toEntity(): BodyMeasurement = BodyMeasurement(
    id = id,
    recordedAt = Instant.fromEpochMilliseconds(recorded_at),
    heightCm = height_cm,
    weightKg = weight_kg,
)
